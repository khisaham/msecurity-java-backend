package com.safaricom.customer;

import com.safaricom.commons.Log;
import com.safaricom.commons.SafHikariConnection;
import com.zaxxer.hikari.HikariDataSource;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TransactionsAPI {

    public static String GetAllTransactions(String payload){
        JSONObject response = new JSONObject();
        JSONObject requested_message = new JSONObject(payload);

        if(requested_message.has("phone_number")){
            String phone_number = requested_message.getString("phone_number");
            String query = "select * from tbl_main_transactions where '"+phone_number+"' in (partya, partyb)";
         //   String new_query = String.format(query,phone_number);
            try {
                HikariDataSource ds = SafHikariConnection.getDataSource();
                Connection conn = ds.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                JSONArray arr = new JSONArray();
                while(rs.next()){
                JSONObject new_response = new JSONObject();
                    new_response.put("receipt", rs.getString("receipt"));
                    new_response.put("amount", rs.getString("amount"));
                    new_response.put("date", rs.getString("date"));

                    arr.put(new_response);
                }
                response.put("All Transactions ", arr);

        }catch(Exception e){
            Log.d("SQL error occured "+e);
        }
        }

        return response.toString();
    }
}
