package com.safaricom.customer;

import com.safaricom.commons.DbBoilerPlates;
import com.safaricom.commons.Log;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Formatter;

public class CustomerAPI {

    public static String NewCustomer(String payload) {
        JSONObject response = new JSONObject();
        JSONObject requested_data = new JSONObject(payload);
        Log.w("Data sent here:"+payload);
        if(requested_data.has("pin") && requested_data.has("surname") && requested_data.has("first_name") && requested_data.has("last_name") && requested_data.has("id_number") && requested_data.has("phone_number")){
//check if a simillar phone_number is already registered
            int is_registered = DbBoilerPlates.DoReturnInt("select * from tbl_customer_details where phone_number ='"+requested_data.getString("phone_number")+"'", "phone_number");
            if(is_registered>0){
                response.put("status", false);
                response.put("reason", "This phone number is already registered. Kindly contact customer care for help");
            }else{

                String pin = requested_data.getString("pin");


                    String secure_pin = encryptPassword(pin);
                    String surname = requested_data.getString("surname");
                    String first_name = requested_data.getString("first_name");
                    String second_name = requested_data.getString("last_name");
                    String id_number = requested_data.getString("id_number");
                    String phone_number = requested_data.getString("phone_number");
                    int j = DbBoilerPlates.DoInserts("insert into tbl_customer_details (pin, surname, first_name, last_name, id_number, phone_number) values ('"+secure_pin+"','"+surname+"','"+first_name+"','"+second_name+"','"+id_number+"','"+phone_number+"') ");
                    if(j>0){
                        response.put("status", true);
                        response.put("reason", "Registration is Successfully");
                    }




            }
        }else{
            response.put("status", false);
            response.put("reason", "ALl fields are mandatory");
        }
        return response.toString();
    }



    public static String NewMobileWalletCustomer(String payload) {
        JSONObject response = new JSONObject();
        JSONObject requested_data = new JSONObject(payload);
        if(requested_data.has("mobile_wallet_pin") && requested_data.has("surname") && requested_data.has("firstname") && requested_data.has("secondname") && requested_data.has("phone_number")){
//check if a simillar phone_number is already registered
            int is_registered = DbBoilerPlates.DoReturnInt("select * from tbl_mobile_wallet_details where phone_number ='"+requested_data.getString("phone_number")+"'", "phone_number");
            if(is_registered>0){
                response.put("status", false);
                response.put("reason", "You already have an account. Contact customer care");
            }else{

                String mobile_wallet_pin = requested_data.getString("mobile_wallet_pin");

                String secure_pin = encryptPassword(mobile_wallet_pin);
                String surname = requested_data.getString("surname");
                String firstname = requested_data.getString("firstname");
                String secondname = requested_data.getString("secondname");
                String phone_number = requested_data.getString("phone_number");
                LocalDateTime reg_date = LocalDateTime.now();
                int j = DbBoilerPlates.DoInserts("insert into tbl_mobile_wallet_details (pin, surname, first_name, last_name, phone_number, reg_date, last_activity) values ('"+secure_pin+"','"+surname+"','"+firstname+"','"+secondname+"','"+phone_number+"','"+reg_date+"','"+reg_date+"') ");
                if(j>0){
                    response.put("status", true);
                    response.put("reason", "Registration is Successfully");
                }
            }
        }else{
            response.put("status", false);
            response.put("reason", "ALl fields are mandatory");
        }
        return response.toString();
    }
    public  static String encryptPassword(String password)
    {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(password.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return sha1;
    }
    private static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
