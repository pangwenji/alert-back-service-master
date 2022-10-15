package com.welab.alertsystem.DAO.Impl;

import com.welab.alertsystem.model.*;
import com.welab.alertsystem.DAO.AlertDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
public class AlertDaoImpl implements AlertDao {

    JdbcTemplate jdbcTemplate;

    @Autowired
    public AlertDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public List<Alert> getAllAlert(Integer offset, Integer limit) {
        String sql = "" +
                "SELECT *, " +
                "COUNT(*) OVER() as total_count ," +
                "null as day_old " +
                "FROM ALERTS " +
                "ORDER BY alert_date DESC " +
                "OFFSET ? " +
                "LIMIT ? ";
        return jdbcTemplate.query(sql, mapAlertFromDb(), new Object[]{offset, limit});
    }

    @Override
    public List<Alert> getAllAlertByCustomerId(String customerId, Integer offset, Integer limit) {
        String sql = "" +
                "SELECT *, " +
                "COUNT(*) OVER() as total_count ," +
                "null as day_old " +
                "FROM ALERTS " +
                "WHERE customer_id = ? " +
                "ORDER BY alert_date DESC " +
                "OFFSET ? " +
                "LIMIT ? ";
        return jdbcTemplate.query(sql, mapAlertFromDb(), new Object[]{customerId, offset, limit});
    }

    @Override
    public List<Alert> getAllAlertByCustomerIdAndStatus(String customerId, String status, Integer offset, Integer limit) {
        String sql = "" +
                "SELECT " +
                "(NOW()::date - alert_date::date) as day_old, " +
                "COUNT(*) OVER()as total_count,  " +
                "* FROM ALERTS  " +
                "WHERE status = ? " +
                "AND customer_id = ? " +
                "ORDER BY alert_date DESC  " +
                "OFFSET ? LIMIT ?";
        return jdbcTemplate.query(sql, mapAlertFromDb(), new Object[]{status, customerId, offset, limit});
    }

    @Override
    public Integer insertApprovalRequest(List<Alert> alertList, Integer assignedMaker) {
        String sql0 = "" +
                "INSERT INTO approval_requests " +
                "(status, assigned_maker_id) " +
                "VALUES (?, ?) " + " RETURNING id";
        List<Integer> list = jdbcTemplate.query(sql0, (resultSet, index) -> {
            return resultSet.getInt(1);
        }, new Object[]{"OPEN", assignedMaker});
        Integer newApprovalId = list.get(0);
        for (int i = 0; i < alertList.size(); i++) { // refactor this to use batch update;
            Alert c = alertList.get(i);
            Integer caseId = c.getId();
            String sql1 = "INSERT INTO alerts__approval_requests (alert_id, approval_request_id) VALUES (?,?) RETURNING id";
            List<Integer> list1 = jdbcTemplate.query(sql1, (resultSet, index) -> {
                return resultSet.getInt(1);
            }, new Object[]{caseId, newApprovalId});
        }
        return list.get(0);
    }

    @Override
    public boolean insertAlertBatch(List<UploadAlert> dataList) {
        jdbcTemplate.batchUpdate("" +
                "INSERT into alerts " +
                "(fcm_alert_id, fcm_case_id, customer_id, rule_id, rule_name, tmxlea_flag, alert_date) "  +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (fcm_alert_id) DO NOTHING", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, dataList.get(i).getFcmAlertId());
                ps.setString(2, dataList.get(i).getFcmCaseId());
                ps.setString(3, dataList.get(i).getCustomerId());
                ps.setObject(4, dataList.get(i).getRuleId(),java.sql.Types.INTEGER);
                ps.setString(5, dataList.get(i).getRuleName());
                ps.setString(6, dataList.get(i).getTmxleaFlag());
                ps.setTimestamp(7, Timestamp.valueOf(dataList.get(i).getAlertDate()));
            }

            @Override
            public int getBatchSize() {
                return dataList.size();
            }
        });
        return true;
    }

    public boolean updateCaseAfterApproval(String fromStatus, String toStatus, Integer approvalRequestId){
        String sql = "with cte as (" +
                "SELECT a.id FROM " +
                "alerts as a " +
                "JOIN alerts__approval_requests as b " +
                "on a.id = b.alert_id " +
                "JOIN approval_requests as c " +
                "on b.approval_request_id = c.id " +
                "WHERE a.status = ? and c.id = ?) " +
                "UPDATE alerts " +
                "SET status = ? " +
                "FROM cte " +
                "WHERE alerts.id = cte.id RETURNING cte.id";
        List<Integer> list1 = jdbcTemplate.query(sql, (resultSet, i) -> {
            return resultSet.getInt("id");
        }, new Object[]{fromStatus, approvalRequestId, toStatus});
        return true;
    }

    @Override
    public boolean updateApprovalRequest(Integer rowId, String fromStatus, String toStatus, String status, Integer makerId, Integer approverId, String comment, String url) {
        String sql = "UPDATE approval_requests " +
                "SET status = ?, " +
                "actual_maker_id = ?, " +
                "assigned_approver_id = ?, " +
                "maker_comment = ?, " +
                "maker_attachment_url = ?, " +
                "request_date = NOW() " +
                "WHERE id = ? RETURNING id";
        List<Integer> list1 = jdbcTemplate.query(sql, (resultSet, i) -> {
            return resultSet.getInt("id");
        }, new Object[]{status, makerId, approverId, comment, url, rowId });


        for(int i = 0; i < list1.size(); i++){
            Integer target = list1.get(i);
            updateCaseAfterApproval(fromStatus, toStatus, target);
        }
        return true;
    }

    @Override
    public List<Alert> getAllAlertByCustomerIdAndStatus(String customerId, List<String> statusList, Integer offset, Integer limit) {
        List<Object> oParams = new ArrayList<>();
        List<String> list = statusList.stream().map(e -> "status = ? ").collect(Collectors.toList());
        String result = String.join(" OR ", list);
        oParams.addAll(statusList);
        oParams.add(customerId);
        oParams.add(offset);
        oParams.add(limit);
        String sql = "" +
                "SELECT " +
                "(NOW()::date - alert_date::date) as day_old, " +
                "COUNT(*) OVER() as total_count,  " +
                "* FROM ALERTS  " +
                "WHERE " + "(" + result + ")" +
                "AND customer_id = ? " +
                "ORDER BY alert_date DESC  " +
                "OFFSET ? LIMIT ?";
        return jdbcTemplate.query(sql, mapAlertFromDb(), oParams.toArray());
    }

    @Override
    public List<GroupedMakerAlert> getCustomerGroupedOpenAlert(String customerId) {
        String sql = "" +
                "WITH tmp as ( " +
                "SELECT MIN(a.id) as id, " +
                "COUNT(*) OVER() as total_count, " +
                "min(customer_id) as customer_id, " +
                "min(alert_date) as open_case_date, " +
                "min(status) as status, " +
                "max(tmxlea_flag) as flag " +
                "FROM alerts as a " +
                "WHERE customer_id = ? " +
                "GROUP BY customer_id " +
                "ORDER BY open_case_date ASC " +
                "LIMIT ?) " +
                "SELECT * FROM ( " +
                "SELECT * , (NOW()::date - open_case_date::date) as day_old, " +
                "(CASE " +
                "WHEN (tmp.flag is not null) and (NOW()::date - open_case_date::date) > 75 then 'CRITICAL' " +
                "WHEN (tmp.flag is not null) or (NOW()::date - open_case_date::date) >= 60 then 'HIGH' " +
                "WHEN (NOW()::date - open_case_date::date) > 30 then 'MEDIUM' " +
                "ELSE 'LOW' " +
                "END) as priority " +
                "FROM tmp) as tmp1 " +
                "ORDER BY array_position(ARRAY['CRITICAL','HIGH','MEDIUM','LOW'], priority)";
        return jdbcTemplate.query(sql, mapGroupedMakerAlertFromDb(), new Object[]{customerId, 1});
    }

    @Override
    public List<GroupedMakerAlert> getAllGroupedOpenAlert(Integer offset, Integer limit) {
        String sql = "" +
                "WITH tmp as ( " +
                "SELECT MIN(a.id) as id, " +
                "COUNT(*) OVER() as total_count, " +
                "min(customer_id) as customer_id, " +
                "min(alert_date) as open_case_date, " +
                "min(status) as status, " +
                "max(tmxlea_flag) as flag " +
                "FROM alerts as a " +
                "WHERE status = 'OPEN' " +
                "GROUP BY customer_id " +
                "ORDER BY open_case_date ASC " +
                "OFFSET ? LIMIT ?) " +
                "SELECT * FROM ( " +
                "SELECT * , (NOW()::date - open_case_date::date) as day_old, " +
                "(CASE " +
                "WHEN (tmp.flag is not null) and (NOW()::date - open_case_date::date) > 75 then 'CRITICAL' " +
                "WHEN (tmp.flag is not null) or (NOW()::date - open_case_date::date) >= 60 then 'HIGH' " +
                "WHEN (NOW()::date - open_case_date::date) > 30 then 'MEDIUM' " +
                "ELSE 'LOW' " +
                "END) as priority " +
                "FROM tmp) as tmp1 " +
                "ORDER BY array_position(ARRAY['CRITICAL','HIGH','MEDIUM','LOW'], priority)";
        return jdbcTemplate.query(sql, mapGroupedMakerAlertFromDb(), new Object[]{offset, limit});
    }

    @Override
    public List<GroupedApproverAlert> getAllGroupedPendingAlert(Integer offset, Integer limit) {

        String sql = "" +
                "With tmp1 as ( " +
                "SELECT MIN(tmp.id) as id, " +
                "COUNT(*) OVER() as total_count,  " +
                "tmp.customer_id as customer_id,  " +
                "min(tmp.assigned_maker) as assigned_maker, " +
                "min(tmp.actual_maker_id) as actual_maker, " +
                "min(tmp.request_date) as request_date, " +
                "min(tmp.status) as status, " +
                "min(tmp.alert_date) as open_case_date, " +
                "min(tmp.maker_comment) as maker_comment, " +
                "min(tmp.maker_attachment_url) as maker_attachment_url, " +
                "max(tmp.flag) as flag " +
                "FROM ( " +
                "SELECT  " +
                "a.id as id, " +
                "a.customer_id,  " +
                "c.request_date as request_date,  " +
                "a.status as status,    " +
                "a.tmxlea_flag as flag, " +
                "a.alert_date as alert_date, " +
                "c.maker_comment as maker_comment, " +
                "c.maker_attachment_url as maker_attachment_url, " +
                "d.username as assigned_maker, " +
                "e.username as actual_maker " +
                "FROM alerts as a  " +
                "join alerts__approval_requests as b on a.id = b.alert_id  " +
                "join approval_requests as c on b.approval_request_id = c.id  " +
                "join users as d on c.assigned_maker_id = d.id " +
                "join users as e on c.actual_maker_id = e.id " +
                "WHERE a.status = 'PENDING' " +
                ") as tmp  " +
                "GROUP BY customer_id " +
                "ORDER BY open_case_date ASC " +
                "OFFSET ? LIMIT ?) " +
                "SELECT * FROM ( " +
                "SELECT * , (NOW()::date - open_case_date::date) as day_old, " +
                "(CASE " +
                " WHEN (tmp1.flag is not null) and (NOW()::date - open_case_date::date) > 75 then 'CRITICAL'  " +
                " WHEN (tmp1.flag is not null) or (NOW()::date - open_case_date::date) >= 60 then 'HIGH'  " +
                " WHEN (NOW()::date - open_case_date::date) > 30 then 'MEDIUM'  " +
                " ELSE 'LOW' " +
                " END) as priority  " +
                "FROM tmp1 ) as tmp2 " +
                "ORDER BY array_position(ARRAY['CRITICAL','HIGH','MEDIUM','LOW'], priority) ";

        return jdbcTemplate.query(sql, mapGroupedApproverAlertFromDb(), new Object[]{offset, limit});
    }

    @Override
    public List<GroupedApproverAlert> getAllGroupedPendingAlertByApproverId(Integer approverId, Integer offset, Integer limit) {
        String sql = "" +
                "With tmp1 as ( " +
                "SELECT MIN(tmp.id) as id, " +
                "COUNT(*) OVER() as total_count,  " +
                "tmp.customer_id as customer_id,  " +
                "min(tmp.assigned_maker) as assigned_maker, " +
                "min(tmp.actual_maker) as actual_maker, " +
                "min(tmp.request_date) as request_date, " +
                "min(tmp.status) as status, " +
                "min(tmp.alert_date) as open_case_date, " +
                "min(tmp.maker_comment) as maker_comment, " +
                "min(tmp.maker_attachment_url) as maker_attachment_url, " +
                "max(tmp.flag) as flag " +
                "FROM ( " +
                "SELECT  " +
                "a.id as id, " +
                "a.customer_id,  " +
                "c.request_date as request_date,  " +
                "a.status as status,    " +
                "a.tmxlea_flag as flag, " +
                "a.alert_date as alert_date, " +
                "c.maker_comment as maker_comment, " +
                "c.maker_attachment_url as maker_attachment_url, " +
                "d.username as assigned_maker, " +
                "e.username as actual_maker " +
                "FROM alerts as a  " +
                "join alerts__approval_requests as b on a.id = b.alert_id  " +
                "join approval_requests as c on b.approval_request_id = c.id  " +
                "join users as d on c.assigned_maker_id = d.id " +
                "join users as e on c.actual_maker_id = e.id " +
                "WHERE a.status = 'PENDING' and c.assigned_approver_id = ? " +
                ") as tmp  " +
                "GROUP BY customer_id " +
                "ORDER BY open_case_date ASC " +
                "OFFSET ? LIMIT ?) " +
                "SELECT * FROM ( " +
                "SELECT * , (NOW()::date - open_case_date::date) as day_old, " +
                "(CASE " +
                " WHEN (tmp1.flag is not null) and (NOW()::date - open_case_date::date) > 75 then 'CRITICAL'  " +
                " WHEN (tmp1.flag is not null) or (NOW()::date - open_case_date::date) >= 60 then 'HIGH'  " +
                " WHEN (NOW()::date - open_case_date::date) > 30 then 'MEDIUM'  " +
                " ELSE 'LOW' " +
                " END) as priority  " +
                "FROM tmp1 " +
                ") as tmp2 " +
                "ORDER BY array_position(ARRAY['CRITICAL','HIGH','MEDIUM','LOW'], priority) ";

        return jdbcTemplate.query(sql, mapGroupedApproverAlertFromDb(), new Object[]{approverId, offset, limit});

    }

    private RowMapper<Alert> mapAlertFromDb() {
        return (resultSet, i) -> {
            Integer id = Optional.ofNullable(resultSet.getString("id")).map(Integer::parseInt).orElse(null);
            String alertId = resultSet.getString("fcm_alert_id");
            String caseId = resultSet.getString("fcm_case_id");
            String customerId = resultSet.getString("customer_id");
            String status = resultSet.getString("status");
            String ruleId = resultSet.getString("rule_id");
            String ruleName = resultSet.getString("rule_name");
            String flag = resultSet.getString("tmxlea_flag");
            Integer totalCount = Optional.ofNullable(resultSet.getString("total_count")).map(Integer::parseInt).orElse(null);
            Integer dayOld = Optional.ofNullable(resultSet.getString("day_old")).map(Integer::parseInt).orElse(null);
            Boolean isRejected = resultSet.getBoolean("is_rejected");
            LocalDateTime requestDate = Optional.ofNullable(resultSet.getTimestamp("request_date")).map(Timestamp::toLocalDateTime).orElse(null);
            LocalDateTime createdAt = Optional.ofNullable(resultSet.getTimestamp("created_at")).map(Timestamp::toLocalDateTime).orElse(null);
            LocalDateTime updatedAt = Optional.ofNullable(resultSet.getTimestamp("updated_at")).map(Timestamp::toLocalDateTime).orElse(null);
            LocalDateTime alertDate = Optional.ofNullable(resultSet.getTimestamp("alert_date")).map(Timestamp::toLocalDateTime).orElse(null);
            Alert alert = new Alert(id, alertId, caseId, customerId, status, ruleId, ruleName, flag, requestDate, createdAt, updatedAt, alertDate, isRejected, dayOld);
            alert.setTotalCount(totalCount);
            return alert;
        };
    }

    private RowMapper<GroupedMakerAlert> mapGroupedMakerAlertFromDb() {
        return (resultSet, i) -> {
            Integer id = Optional.ofNullable(resultSet.getString("id")).map(Integer::parseInt).orElse(null);
            String customerId = resultSet.getString("customer_id");
            Integer totalCount = Optional.ofNullable(resultSet.getString("total_count")).map(Integer::parseInt).orElse(null);
            LocalDateTime openDate = Optional.ofNullable(resultSet.getTimestamp("open_case_date")).map(Timestamp::toLocalDateTime).orElse(null);
            String status = resultSet.getString("status");
            String flag = resultSet.getString("flag");
            Integer dayOld = resultSet.getInt("day_old");
            String priority = resultSet.getString("priority");
            GroupedMakerAlert alert = new GroupedMakerAlert(id, customerId, dayOld, openDate, priority, status, flag);
            alert.setTotalCount(totalCount);
            return alert;
        };
    }

    private RowMapper<GroupedApproverAlert> mapGroupedApproverAlertFromDb() {
        return (resultSet, i) -> {
            Integer id = Optional.ofNullable(resultSet.getString("id")).map(Integer::parseInt).orElse(null);
            Integer totalCount = Optional.ofNullable(resultSet.getString("total_count")).map(Integer::parseInt).orElse(null);
            String customerId = resultSet.getString("customer_id");
            String assignedMaker = resultSet.getString("assigned_maker");
            String actualMaker = resultSet.getString("actual_maker");
            LocalDateTime requestDate = Optional.ofNullable(resultSet.getTimestamp("request_date")).map(Timestamp::toLocalDateTime).orElse(null);
            String status = resultSet.getString("status");
            LocalDateTime openCaseDate = Optional.ofNullable(resultSet.getTimestamp("open_case_date")).map(Timestamp::toLocalDateTime).orElse(null);
            String makerAttachmentUrl = resultSet.getString("maker_attachment_url");
            String makerComment = resultSet.getString("maker_comment");
            String flag = resultSet.getString("flag");
            Integer dayOld = resultSet.getInt("day_old");
            String priority = resultSet.getString("priority");
            GroupedApproverAlert approverAlert = new GroupedApproverAlert(id, customerId, assignedMaker, actualMaker, makerComment, makerAttachmentUrl, status, priority, requestDate, openCaseDate);
            approverAlert.setTotalCount(totalCount);
            return approverAlert;
        };
    }
}
