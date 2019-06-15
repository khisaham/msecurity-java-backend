package com.safaricom.customer;

import com.safaricom.commons.DbBoilerPlates;
import com.safaricom.commons.Log;
import org.json.JSONObject;

import static com.safaricom.customer.CustomerAPI.encryptPassword;

/**
 * check customer pin before authorizing any transaction using pin and phone_number
 */
public class CustomerApiLogin {
    public static JSONObject LoginHandler(String requestMessage){
        Log.w("Login endpoint clicked: "+requestMessage);
    JSONObject response = new JSONObject();
    JSONObject payload = new JSONObject(requestMessage);
        String pin = payload.getString("pin");
        String phone_number = payload.getString("phone_number");
            String secure_pin = encryptPassword(pin);
            int pin_validity = DbBoilerPlates.DoReturnInt("select * from tbl_customer_details where phone_number ='"+phone_number+"' and pin ='"+secure_pin+"'", "id");
            System.out.println(secure_pin+" "+phone_number);
            if(pin_validity>0){
                response.put("phone_number", phone_number);
                response.put("surname", DbBoilerPlates.DoReturnString("select * from tbl_customer_details where phone_number ='"+phone_number+"' and pin ='"+secure_pin+"'", "surname"));
                response.put("first_name", DbBoilerPlates.DoReturnString("select * from tbl_customer_details where phone_number ='"+phone_number+"' and pin ='"+secure_pin+"'", "first_name"));
                response.put("last_name", DbBoilerPlates.DoReturnString("select * from tbl_customer_details where phone_number ='"+phone_number+"' and pin ='"+secure_pin+"'", "last_name"));
                response.put("json_web_acces_token", secure_pin+"@"+phone_number);
                response.put("status", true);

            }else{
                response.put("status", false);
                response.put("reason", "PIN/Phone Number mismatch");

            }
        Log.w("Login data: "+response.toString());
            return response;
    }

    public static String ChangePin(String payload_message){

        JSONObject new_response = new JSONObject();
        JSONObject requested=  new JSONObject(payload_message);
        String secure_pin = encryptPassword(requested.getString("pin"));
       boolean j = DbBoilerPlates.DoUpdates("update tbl_customer_details set pin = '"+secure_pin+"' where phone_number = "+requested.getString("phone_number")+"");
if(j){
    new_response.put("status", true);
    new_response.put("reason", "PIN changed");
}else{
    new_response.put("status", false);
    new_response.put("reason", "Error updating your PIN");
}
        return new_response.toString();
    }
}
