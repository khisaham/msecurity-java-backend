package com.safaricom.customer;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.safaricom.commons.DbBoilerPlates;
import com.safaricom.commons.Log;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static com.safaricom.customer.CustomerApiLogin.LoginHandler;

/**
 *
 * For c2c transactions
 */
public class MoneyTransfer {
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-m-d");
    private static int MAX_UNREGISTERED_AMOUNT = 30000;
    private  static int MAX_REGISTERED_AMOUNT = 70000;
    public static String CustomerToCustomer(String request_message){
        Log.d("Message:"+request_message);
        JSONObject response = new JSONObject();
        JSONObject payload = new JSONObject(request_message);
        if(payload.has("sender_pin") && payload.has("sender_phone") && payload.has("amount") && payload.has("receiver_phone") ) {
            if (payload.length() > 0) {
                String receipt = UUID.randomUUID().toString();
                String id = "insert";
                LocalDateTime today = LocalDateTime.now();
               // String sender_id = payload.getString("sender_id");
                String sender_pin = payload.getString("sender_pin");
                String sender_phone = payload.getString("sender_phone");
                double amount_to_send = Double.parseDouble(payload.getString("amount"));

                String receiver_phone = payload.getString("receiver_phone");
                //validate the phone numbers
                String valid_sender_phone = getInternationalPhone(sender_phone, "KE");
                String valid_reciceiver_phone = getInternationalPhone(receiver_phone, "KE");
                if(valid_reciceiver_phone =="1"){
                    double charges = DbBoilerPlates.DoReturnDouble("select * from tbl_charges where '"+amount_to_send+"' in(min, max)", "send_to_registered");
                    response.put("status", false);
                    response.put("reason", "The phone number your sending to is not valid, please correct to continue ");
                }else {
                    /**check pin validity **/
                    JSONObject loginData = new JSONObject();
                    loginData.put("pin", sender_pin);
                    loginData.put("phone_number", sender_phone);
                    if (LoginHandler(loginData.toString()).getBoolean("status")) {
                        int receiver_id = DbBoilerPlates.DoReturnInt("select * from tbl_customer_details where phone_number = '"+receiver_phone+"'", "id");
    //check balance
                        double balance = DbBoilerPlates.DoReturnDouble("select bal from tbl_customer_details where phone_number ='"+sender_phone+"'", "bal");

                        /**check if the person is registered**/
                        if(receiver_id > 0){
                            double charges = DbBoilerPlates.DoReturnDouble("SELECT * FROM `tbl_charges` WHERE min<"+amount_to_send+" and max>"+amount_to_send+"", "send_to_registered");
                            if(balance > (amount_to_send+charges)){
                                if((amount_to_send+charges) < MAX_REGISTERED_AMOUNT){
                                    int insert_data = DbBoilerPlates.DoInserts("insert into tbl_main_transactions (request_id, receipt, partya, partyb, amount, date, status, charge) values ('"+id+"','"+receipt+"','"+sender_phone+"','"+receiver_phone+"','"+amount_to_send+"','"+today+"','1','"+charges+"')");
                                    if(insert_data>0){
                                        String transaction_type = "debit";
                                        String in = InsertTransaction(receipt,sender_phone, charges, transaction_type, receiver_phone, amount_to_send);
                                        if(in == "done"){
                                            response.put("status", true);
                                            response.put("amount", amount_to_send);
                                            response.put("charges", charges);
                                            response.put("reason", "Transaction Successful ");
                                        }
                                    }
                                    else{
                                        response.put("status", false);
                                        response.put("reason", "Transaction Failed");
                                    }
                                }else {
                                    response.put("status", false);
                                    response.put("reason", "You are not allowed to transact more than "+MAX_REGISTERED_AMOUNT);
                                }
                            }else{
                                response.put("status", false);
                                response.put("reason", "You have insufficient funds to complete this transaction");
                            }

                        }
                        else{
                            double charges = DbBoilerPlates.DoReturnDouble("SELECT * FROM `tbl_charges` WHERE min<"+amount_to_send+" and max>"+amount_to_send+"", "send_to_unregistered");
                            if(balance > charges+amount_to_send){
                                if(amount_to_send+charges < MAX_UNREGISTERED_AMOUNT){

                                    //insert data to tbl_main_transaction table
                                    int insert_data = DbBoilerPlates.DoInserts("insert into tbl_main_transactions (request_id, receipt, partya, partyb, amount, date, status, charge) values ('"+id+"','"+receipt+"','"+sender_phone+"','"+receiver_phone+"','"+amount_to_send+"','"+today+"','1','"+charges+"')");
                                    if(insert_data>0){
                                        //create subtransactions for both sender and receiver
                                        double whole_amount = amount_to_send+charges;
                                        DbBoilerPlates.DoInserts("insert into tbl_subtransactions (request_id, receipt, phone, amount, amount_type, transaction_type) values ('charges','"+receipt+"','"+sender_phone+"','-"+charges+"','charges','debit') ");
                                        DbBoilerPlates.DoInserts("insert into tbl_subtransactions (request_id, receipt, phone, amount, amount_type, transaction_type) values ('credited','"+receipt+"','"+receiver_phone+"','"+amount_to_send+"','Transaction_amount','credit') ");
                                        DbBoilerPlates.DoUpdates("update tbl_customer_details set bal = bal- "+whole_amount+" where phone_number = '"+sender_phone+"'");

                                        response.put("status", true);
                                        response.put("amount", amount_to_send);
                                        response.put("charges", charges);
                                        response.put("reason", "Transaction Successful ");
                                    }
                                    else{
                                        response.put("status", false);
                                        response.put("reason", "Transaction Failed");
                                    }
                                }else {
                                    response.put("status", false);
                                    response.put("reason", "You are not allowed to transact more than "+MAX_UNREGISTERED_AMOUNT);
                                }
                            }else{
                                response.put("status", false);
                                response.put("reason", "You have insufficient funds to complete this transaction");
                            }

                        }

                    }else{
                        response.put("status", false);
                        response.put("reason", "Wrong Pin Entered");
                    }
                }
            } else {
                response.put("status", false);
                response.put("reason", "Transaction failed");
            }
        }else{
            response.put("status", false);
            response.put("reason", "You are not authorized to carry out transactions");
        }


        return response.toString();

    }

    /**
     * To validate phone number
     * @param PassedphoneNumber
     * @param PassedCountry
     * @return
     */
    public static String getInternationalPhone(String PassedphoneNumber, String PassedCountry) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String NewphoneNumber = "1";

        try {
            Phonenumber.PhoneNumber phoneNumberFormated = phoneUtil.parse(PassedphoneNumber, PassedCountry);

            if (phoneUtil.isValidNumber(phoneNumberFormated)) {
                NewphoneNumber = phoneUtil.format(phoneNumberFormated, PhoneNumberUtil.PhoneNumberFormat.E164);

            }

        } catch (NumberParseException e) {
            Log.e("NumberParseException was thrown", e);

        }

        return NewphoneNumber;
    }

    public static String InsertTransaction(String receipt, String sender_phone, double charges, String transaction_type, String receiver_phone, double amount_to_send){
        double whole_amount = amount_to_send+charges;
        int j = DbBoilerPlates.DoInserts("insert into tbl_subtransactions (request_id, receipt, phone, amount, amount_type, transaction_type) values ('charges','"+receipt+"','"+sender_phone+"','-"+charges+"','charges','"+transaction_type+"') ");
        if(j>0){
            DbBoilerPlates.DoInserts("insert into tbl_subtransactions (request_id, receipt, phone, amount, amount_type, transaction_type) values ('credited','"+receipt+"','"+receiver_phone+"','"+amount_to_send+"','Transaction_amount','credit') ");
            DbBoilerPlates.DoUpdates("update tbl_customer_details set bal = bal- "+whole_amount+" where phone_number = '"+sender_phone+"'");
            DbBoilerPlates.DoUpdates("update tbl_customer_details set bal = bal+ "+amount_to_send+" where phone_number = '"+receiver_phone+"'");

        }

    return "done";
    }

    public static String GetCustomerBalance(String payload){
        JSONObject response = new JSONObject();
        JSONObject request =  new JSONObject(payload);
        double balance = DbBoilerPlates.DoReturnDouble("select * from tbl_customer_details where phone_number ='"+request.getString("phone_number")+"'", "bal");
        response.put("phone_number", request.getString("phone_number"));
        response.put("balance", balance);
        return response.toString();
    }
}
