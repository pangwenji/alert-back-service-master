package com.welab.alertsystem.DAO.Impl;

import com.welab.alertsystem.model.ApprovalRequest;
import com.welab.alertsystem.DAO.ApprovalRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ApprovalRequestDaoImpl implements ApprovalRequestDao {

    JdbcTemplate jdbcTemplate;

    @Autowired
    public ApprovalRequestDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existByCustomerIdAndStatus(String customerId, List<String> statusList) {
        List<Object> oParams = new ArrayList<>();
        oParams.add(customerId);
        oParams.addAll(statusList);
        List<String> list = statusList.stream().map(e -> "c.status = ? ").collect(Collectors.toList());
        String result = String.join(" OR ", list);
        String sql = "" +
                "SELECT 1 from " +
                "alerts as a join alerts__approval_requests as b " +
                "on a.id = b.alert_id " +
                "join approval_requests as c " +
                "on b.approval_request_id = c.id " +
                "where a.customer_id= ? and (" + result + ") ";
        List<Integer> resultList = jdbcTemplate.query(sql, (resultSet, i) -> {
            return 1;
        }, oParams.toArray());
        return !resultList.isEmpty();
    }


    @Override
    public List<ApprovalRequest> getGroupApprovalRequestByCustomerId(String customerId, Integer offset, Integer limit) {
        String sql = "" +
                "WITH tmp as ( " +
                "SELECT c.id as id," +
                "COUNT(*) OVER() as total_count, " +
                "MIN(c.status) as status, " +
                "MIN(c.request_date) as request_date, " +
                "MIN(c.assigned_maker_id) as assigned_maker_id, " +
                "MIN(c.actual_maker_id) as actual_maker_id, " +
                "MIN(c.assigned_approver_id) as assigned_approver_id, " +
                "MIN(c.actual_approver_id) as actual_approver_id, " +
                "MIN(c.maker_comment) as maker_comment, " +
                "MIN(c.maker_attachment_url) as maker_attachment_url, " +
                "MIN(c.approver_comment) as approver_comment, " +
                "MIN(c.approver_action_date) as approver_action_date, " +
                "STRING_AGG(a.fcm_case_id,';') as case_list " +
                "FROM " +
                "alerts as a  " +
                "JOIN alerts__approval_requests as b " +
                "on a.id = b.alert_id  " +
                "JOIN approval_requests as c " +
                "on b.approval_request_id = c.id " +
                "WHERE a.customer_id = ? " +
                "GROUP BY c.id ) " +
                "SELECT tmp.*, COUNT(*) OVER() as total_count, " +
                "d.username as assigned_maker,  " +
                "e.username as actual_maker, " +
                "f.username as assigned_approver, " +
                "g.username as actual_approver " +
                "FROM tmp " +
                "LEFT JOIN users as d " +
                "on tmp.assigned_maker_id = d.id " +
                "LEFT JOIN users as e " +
                "on tmp.actual_maker_id = e.id " +
                "LEFT JOIN users as f " +
                "on tmp.assigned_approver_id = f.id " +
                "LEFT JOIN users as g " +
                "on tmp.actual_approver_id = g.id " +
                "ORDER BY tmp.request_date DESC " +
                "OFFSET ? LIMIT ? ";
        return jdbcTemplate.query(sql, mapApprovalAlertFromDb(), new Object[]{customerId, offset, limit});
    }

    @Override
    public List<Integer> getApprovalRequestIdByCustomerIdAndStatus(String customerId, String status,Integer offset, Integer limit) {
        String sql = "SELECT c.id as id from " +
                "alerts as a join alerts__approval_requests as b " +
                "on a.id = b.alert_id " +
                "join approval_requests as c " +
                "on b.approval_request_id = c.id " +
                "where  a.customer_id= ? and c.status = ? " +
                "OFFSET ? LIMIT ?";
        List<Integer> list = jdbcTemplate.query(sql, (resultSet, i) -> {
            return resultSet.getInt("id");
        }, new Object[]{customerId, status, offset, limit});
        return list;
    }

    @Override
    public void updateApprovalRequest(Integer rowId, String comment,Integer approverId, boolean isAccept) {
        String newStatus = isAccept ? "ACCEPT": "REJECT";
        String sql = "UPDATE approval_requests " +
                "SET status = ?, " +
                "approver_comment = ?, " +
                "actual_approver_id = ?, " +
                "approver_action_date = NOW() " +
                "WHERE id = ? RETURNING id ";
        List<Integer> list1 = jdbcTemplate.query(sql, (resultSet, i) -> {
            return resultSet.getInt("id");
        }, new Object[]{newStatus, comment, approverId, rowId});
        String newStatus1 = isAccept ? "CLOSE": "OPEN";
        for(int i = 0; i < list1.size(); i++){
            Integer target = list1.get(i);
            updateCaseAfterApproval("PENDING",newStatus1, target, !isAccept);
        }
    }

        public boolean updateCaseAfterApproval(String fromStatus, String toStatus, Integer approvalRequestId, boolean is_rej){
        String sql = "with cte as (SELECT a.id FROM " +
                "alerts as a " +
                "JOIN alerts__approval_requests as b " +
                "on a.id = b.alert_id " +
                "JOIN approval_requests as c " +
                "on b.approval_request_id = c.id " +
                "WHERE a.status = ? and c.id = ?) " +
                "UPDATE alerts " +
                "SET status = ?," +
                "is_rejected = ? " +
                "FROM cte " +
                "WHERE alerts.id = cte.id RETURNING cte.id";
        List<Integer> list1 = jdbcTemplate.query(sql, (resultSet, i) -> {
            return resultSet.getInt("id");
        }, new Object[]{fromStatus, approvalRequestId, toStatus, is_rej});

        return true;
    }


    private RowMapper<ApprovalRequest> mapApprovalAlertFromDb() {
        return (resultSet, i) -> {
            Integer id = Optional.ofNullable(resultSet.getString("id")).map(Integer::parseInt).orElse(null);
            Integer totalCount = Optional.ofNullable(resultSet.getString("total_count")).map(Integer::parseInt).orElse(null);
            String status = resultSet.getString("status");
            String assignedMaker = resultSet.getString("assigned_maker");
            String actualMaker = resultSet.getString("actual_maker");
            String assignedApprover = resultSet.getString("assigned_approver");
            String actualApprover = resultSet.getString("actual_approver");
            String makerComment = resultSet.getString("maker_comment");
            String makerAttachment = resultSet.getString("maker_attachment_url");
            String approverComment = resultSet.getString("approver_comment");
            String caseList = resultSet.getString("case_list");
            LocalDateTime requestDate = Optional.ofNullable(resultSet.getTimestamp("request_date")).map(Timestamp::toLocalDateTime).orElse(null);
            LocalDateTime approverActionDate = Optional.ofNullable(resultSet.getTimestamp("approver_action_date")).map(Timestamp::toLocalDateTime).orElse(null);
            ApprovalRequest approvalRequest = new ApprovalRequest(id, status, assignedMaker, actualMaker, assignedApprover, actualApprover, makerComment, makerAttachment, approverComment, caseList, requestDate, approverActionDate);
            approvalRequest.setTotalCount(totalCount);
            return approvalRequest;
        };
    }
}
