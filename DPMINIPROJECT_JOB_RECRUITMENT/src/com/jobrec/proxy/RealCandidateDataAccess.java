package com.jobrec.proxy;

import com.jobrec.db.DBConnection;
import com.jobrec.model.Candidate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// REAL SUBJECT — does the actual, "expensive"/sensitive work of pulling
// candidate details straight from the database.
public class RealCandidateDataAccess implements CandidateDataAccess {

    @Override
    public Candidate getCandidateDetails(int candidateId) {
        Connection conn = DBConnection.getInstance().getConnection();
        String sql = "SELECT * FROM candidates WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Candidate(
                        rs.getInt("id"), rs.getString("name"), rs.getInt("age"),
                        rs.getString("education"), rs.getString("skills"),
                        rs.getString("experience"), rs.getString("location"),
                        rs.getDouble("expected_salary"));
            }
        } catch (Exception e) {
            System.out.println("Error fetching candidate: " + e.getMessage());
        }
        return null;
    }
}
