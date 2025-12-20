package com.billtracker.dao;

import com.billtracker.model.Group;
import com.billtracker.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

    public void createGroup(Group group) throws SQLException {
        String sql = "INSERT INTO `groups` (id, name) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, group.getId());
            pstmt.setString(2, group.getName());

            pstmt.executeUpdate();
        }
    }

    public void addGroupMember(String groupId, String userId) throws SQLException {
        String sql = "INSERT INTO group_members (group_id, user_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            pstmt.setString(2, userId);

            pstmt.executeUpdate();
        }
    }

    public Group getGroup(String groupId) throws SQLException {
        String sqlGroup = "SELECT * FROM `groups` WHERE id = ?";
        String sqlMembers = "SELECT u.* FROM users u JOIN group_members gm ON u.id = gm.user_id WHERE gm.group_id = ?";

        Group group = null;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sqlGroup)) {
            pstmt.setString(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    group = new Group(rs.getString("id"), rs.getString("name"));
                }
            }
        }

        if (group != null) {
            try (Connection conn = DBConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sqlMembers)) {
                pstmt.setString(1, groupId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        User user = new User(
                                rs.getString("id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("phone"));
                        group.addUser(user);
                    }
                }
            }
        }
        return group;
    }

    public List<Group> getAllGroups() throws SQLException {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT * FROM `groups`";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                groups.add(new Group(rs.getString("id"), rs.getString("name")));
            }
        }
        return groups;
    }
}
