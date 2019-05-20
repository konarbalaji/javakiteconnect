package com.trading.application;

import com.com.selenium.web.RequestToken;
import com.neovisionaries.ws.client.WebSocketException;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.SessionExpiryHook;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.User;
import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TradeExecutor {

    //Logger logger = Logger.getLogger(TradeExecutor.class.getName());

    public static void main(String[] args){
        try {
            // First you should get request_token, public_token using kitconnect login and then use request_token, public_token, api_secret to make any kiteConnect api call.
            // Initialize KiteSdk with your apiKey.
            //KiteConnect kiteConnect = new KiteConnect("ukm5op3tp70x5bl2");

            //If you wish to enable debug logs send true in the constructor, this will log request and response.
            KiteConnect kiteConnect = new KiteConnect("", true);

            // If you wish to set proxy then pass proxy as a second parameter in the constructor with api_key. syntax:- new KiteConnect("xxxxxxyyyyyzzz", proxy).
            //KiteConnect kiteConnect = new KiteConnect("xxxxyyyyzzzz", userProxy, false);

            // Set userId
            kiteConnect.setUserId("MS9491");

            // Get login url
            final String url = kiteConnect.getLoginURL();

            // Set session expiry callback.
            kiteConnect.setSessionExpiryHook(new SessionExpiryHook() {
                @Override
                public void sessionExpired() {
                    System.out.println("session expired");
                    String accessToken = new RequestToken().getRequestToken(url);
                }
            });

                /* The request token can to be obtained after completion of login process. Check out https://kite.trade/docs/connect/v1/#login-flow for more information.
                   A request token is valid for only a couple of minutes and can be used only once. An access token is valid for one whole day. Don't call this method for every app run.
                   Once an access token is received it should be stored in preferences or database for further usage.
                 */
            String requestToken = ""; // = new RequestToken().getRequestToken(url);
            String apiSecret = "";

            User user =  kiteConnect.generateSession(requestToken, apiSecret);
            kiteConnect.setAccessToken(user.accessToken);
            kiteConnect.setPublicToken(user.publicToken);

            ExampleImpl exampleImpl = new ExampleImpl();

            exampleImpl.getProfile(kiteConnect);

            exampleImpl.getMargins(kiteConnect);

            List<Instrument> listOfInst =  exampleImpl.getInstrumentsForExchange(kiteConnect);

            /* Find Date of next thursday(If not today) */
            Calendar nextThursdDate = Calendar.getInstance();
            while (nextThursdDate.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) {
                nextThursdDate.add(Calendar.DATE, 1);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String nextThursdDateStr = sdf.format(nextThursdDate.getTime());

            List<Instrument> bankNiftyOptions = new ArrayList<Instrument>();
            List<String> tradingSymboles = new ArrayList<String>();

            for(Instrument instr : listOfInst){

                if(instr.getTradingsymbol().contains("BANKNIFTY") && instr.segment.equals("NFO-OPT"))
                {
                    String instExpiryDate = sdf.format(instr.getExpiry().getTime());

                    if(instExpiryDate.equals(nextThursdDateStr)){
                            System.out.println( "Instr Name : " + instr.getTradingsymbol());
                            bankNiftyOptions.add(instr);tradingSymboles.add(instr.getTradingsymbol());
                    }
                }
            }


            for(Instrument instr : bankNiftyOptions){

                String[] instr_exch_token = {new Long(instr.getExchange_token()).toString()};

                exampleImpl.getQuote(kiteConnect, instr_exch_token);
                exampleImpl.getOHLC(kiteConnect,instr_exch_token);
                exampleImpl.getLTP(kiteConnect,instr_exch_token);
                exampleImpl.getTriggerRange(kiteConnect,instr_exch_token);
                exampleImpl.getHistoricalData(kiteConnect, instr.getExchange_token());

            }

            //String orderId = exampleImpl.placeOrder(kiteConnect);

            //exampleImpl.modifyOrder(kiteConnect, orderId);

            //exampleImpl.cancelOrder(kiteConnect, orderId);

            exampleImpl.placeBracketOrder(kiteConnect);

            exampleImpl.modifyFirstLegBo(kiteConnect);

            exampleImpl.modifySecondLegBoSLM(kiteConnect);

            exampleImpl.modifySecondLegBoLIMIT(kiteConnect);

            exampleImpl.exitBracketOrder(kiteConnect);

            //exampleImpl.getTriggerRange(kiteConnect);

            exampleImpl.placeCoverOrder(kiteConnect);

            exampleImpl.converPosition(kiteConnect);

            //exampleImpl.getHistoricalData(kiteConnect);

            exampleImpl.getOrders(kiteConnect);

            exampleImpl.getOrder(kiteConnect);

            exampleImpl.getTrades(kiteConnect);

            exampleImpl.getTradesWithOrderId(kiteConnect);

            exampleImpl.getPositions(kiteConnect);

            exampleImpl.getHoldings(kiteConnect);

            exampleImpl.getAllInstruments(kiteConnect);

            exampleImpl.getInstrumentsForExchange(kiteConnect);

            /*exampleImpl.getQuote(kiteConnect);

            exampleImpl.getOHLC(kiteConnect);

            exampleImpl.getLTP(kiteConnect);*/

            exampleImpl.getMFInstruments(kiteConnect);

            exampleImpl.placeMFOrder(kiteConnect);

            exampleImpl.cancelMFOrder(kiteConnect);

            exampleImpl.getMFOrders(kiteConnect);

            exampleImpl.getMFOrder(kiteConnect);

            exampleImpl.placeMFSIP(kiteConnect);

            exampleImpl.modifyMFSIP(kiteConnect);

            exampleImpl.cancelMFSIP(kiteConnect);

            exampleImpl.getMFSIPS(kiteConnect);

            exampleImpl.getMFSIP(kiteConnect);

            exampleImpl.getMFHoldings(kiteConnect);

            exampleImpl.logout(kiteConnect);

            ArrayList<Long> tokens = new ArrayList<>();
            tokens.add(Long.parseLong("256265"));
            exampleImpl.tickerUsage(kiteConnect, tokens);
        } catch (KiteException e) {
            System.out.println(e.message+" "+e.code+" "+e.getClass().getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        } catch (WebSocketException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void findIntheMoney(String scripName){
        try{

        }catch(Exception e){

        }
    }

}
