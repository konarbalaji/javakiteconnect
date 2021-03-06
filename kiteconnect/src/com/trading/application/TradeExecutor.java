package com.trading.application;

import com.trading.application.strategy.AdhocStrategy;
import com.trading.application.utils.OHLCData;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.SessionExpiryHook;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.*;

import org.apache.bcel.generic.INSTANCEOF;
import org.apache.log4j.Level;
import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.Logger;

public class TradeExecutor {

    public ExampleImpl exampleImpl = new ExampleImpl();
    public Instrument bankNiftyFuture = null;
    public KiteConnect kiteConnect = kiteConnect = new KiteConnect("ukm5op3tp70x5bl2", true);
    public List<Instrument> listOfFNOInst = null;
    public List<Instrument> bankNiftyOptions = new ArrayList<Instrument>();
    public Map<String, Long> callMoneynessMap = new HashMap<String, Long>();
    public Map<String, Long> putMoneynessMap = new HashMap<String, Long>();
    public Map<String, Instrument> bankNiftyStkToInstrMap = null;
    public String futureName = "";
    public Map<String, Instrument> bankNiftyOptionsMap = new HashMap<String, Instrument>();
    public Map<String, Order> orderTracker = new HashMap<String, Order>();
    public int noOfProfitableOrders = 0;
    public OHLCData lastOhlc = new OHLCData();
    public List<Double> lastMinPriceList = new ArrayList<>();

    Logger logger = Logger.getLogger(TradeExecutor.class.getName());

    public static void main(String[] args){

        try{
            //BasicConfigurator.configure();
            TradeExecutor trdExec = new TradeExecutor();
            trdExec.setUpData();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void setUpData(){

        logger.info("-------------- Inside setUpData ---------------------");

        try {
            // First you should get request_token, public_token using kitconnect login and then use request_token, public_token, api_secret to make any kiteConnect api call.
            // Initialize KiteSdk with your apiKey.
            //KiteConnect kiteConnect = new KiteConnect("ukm5op3tp70x5bl2");

            //If you wish to enable debug logs send true in the constructor, this will log request and response.


            // If you wish to set proxy then pass proxy as a second parameter in the constructor with api_key. syntax:- new KiteConnect("xxxxxxyyyyyzzz", proxy).
            //KiteConnect kiteConnect = new KiteConnect("xxxxyyyyzzzz", userProxy, false);

            // Set userId
            kiteConnect.setUserId("MS9491");
            logger.info("Loged in as MS9491");

            // Get login url
            final String url = kiteConnect.getLoginURL();

            // Set session expiry callback.
            kiteConnect.setSessionExpiryHook(new SessionExpiryHook() {
                @Override
                public void sessionExpired() {
                    logger.info("session expired");
                }
            });

                /* The request token can to be obtained after completion of login process. Check out https://kite.trade/docs/connect/v1/#login-flow for more information.
                   A request token is valid for only a couple of minutes and can be used only once. An access token is valid for one whole day. Don't call this method for every app run.
                   Once an access token is received it should be stored in preferences or database for further usage.
                 */
            String requestToken = "";
            String apiSecret = "df1wnphuf8f2n5k43m9afey94m3ehphn";

            User user =  kiteConnect.generateSession(requestToken, apiSecret);
            kiteConnect.setAccessToken(user.accessToken);
            kiteConnect.setPublicToken(user.publicToken);

            exampleImpl.getProfile(kiteConnect);

            //exampleImpl.getMargins(kiteConnect);

            listOfFNOInst =  exampleImpl.getInstrumentsForExchange(kiteConnect,"NFO");

            /* Find Date of next thursday(If not today) */
            Calendar nextThursdDate = Calendar.getInstance();
            while (nextThursdDate.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) {
                nextThursdDate.add(Calendar.DATE, 1);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String nextThursdDateStr = sdf.format(nextThursdDate.getTime());

            SimpleDateFormat sdfMonth = new SimpleDateFormat("MMM");
            String currentMonth = sdfMonth.format(Calendar.getInstance().getTime());

            //List<String> tradingSymbols = new ArrayList<String>();
            futureName = "BANKNIFTY19" + currentMonth.toUpperCase() + "FUT";
            //futureName = "BANKNIFTY19JULFUT";

            for(Instrument instr : listOfFNOInst){

                //logger.info( "Instr Name : " + instr.getTradingsymbol());

                if(instr.getTradingsymbol().contains("BANKNIFTY") && instr.segment.equals("NFO-OPT"))
                {
                    String instExpiryDate = sdf.format(instr.getExpiry().getTime());

                    if(instExpiryDate.equals(nextThursdDateStr)){
                        //logger.info( "Instr Name : " + instr.getTradingsymbol());
                        bankNiftyOptions.add(instr);
                        //tradingSymbols.add(instr.getTradingsymbol());
                        bankNiftyOptionsMap.put(instr.getTradingsymbol(),instr);
                        //bankNiftyStkToInstrMap.put(instr.getStrike(),instr);
                    }
                }

                if(instr.getTradingsymbol().contains(futureName) && instr.segment.equals("NFO-FUT")){
                    bankNiftyFuture=instr;
                }
            }

            callMoneynessMap.putAll(findMoneyNess(futureName, Constants.CALL_OPTION));
            putMoneynessMap.putAll(findMoneyNess(futureName, Constants.PUT_OPTION));

            //executeStraddle();

            /*AdhocStrategy adhoc = new AdhocStrategy();
            adhoc.adHocStrategy();*/

            }catch (KiteException e) {
                logger.error(e.getMessage()+" "+" "+e.getClass().getName());
                logger.log(Level.ERROR, e.getMessage(), e);
            } catch (JSONException e) {
                logger.error(e.getMessage()+" "+" "+e.getClass().getName());
                logger.log(Level.ERROR, e.getMessage(), e);
            }catch (IOException e) {
                logger.error(e.getMessage()+" "+" "+e.getClass().getName());
                logger.log(Level.ERROR, e.getMessage(), e);
            } catch(Exception e){
                logger.error(e.getMessage()+" "+" "+e.getClass().getName());
                logger.log(Level.ERROR, e.getMessage(), e);
            }

    }

    public Map<String, Long> findMoneyNess(String scripName, String callPutIndicator){

        long atTheMoney = 0;
        Map<String, Long> moneynessMap = new HashMap<String, Long>();

        try{

            double ltp = exampleImpl.getLTPOfSingleInstrument(kiteConnect , bankNiftyFuture);
            atTheMoney = Math.round(ltp/100)*100;

            /*if(ltp%100 > 50){
                double quot = ltp/100;
                atTheMoney = Math.round(quot);
            }else{
                double quot = ltp/100;
                quot = Math.abs(quot);
                atTheMoney =   (quot*100);
            }*/

            logger.info("Price of ATM Strike - "+bankNiftyFuture.getTradingsymbol()+" >> " + atTheMoney);;
            if(callPutIndicator.equals(Constants.CALL_OPTION)){

                moneynessMap.put(Constants.ITM5,atTheMoney-500);
                moneynessMap.put(Constants.ITM4,atTheMoney-400);
                moneynessMap.put(Constants.ITM3,atTheMoney-300);
                moneynessMap.put(Constants.ITM2,atTheMoney-200);
                moneynessMap.put(Constants.ITM1,atTheMoney-100);
                moneynessMap.put(Constants.ATM,atTheMoney);
                moneynessMap.put(Constants.OTM1,atTheMoney+100);
                moneynessMap.put(Constants.OTM2,atTheMoney+200);
                moneynessMap.put(Constants.OTM3,atTheMoney+300);
                moneynessMap.put(Constants.OTM4,atTheMoney+400);
                moneynessMap.put(Constants.OTM5,atTheMoney+500);

            }else if(callPutIndicator.equals(Constants.PUT_OPTION)){
                moneynessMap.put(Constants.OTM5,atTheMoney-500);
                moneynessMap.put(Constants.OTM4,atTheMoney-400);
                moneynessMap.put(Constants.OTM3,atTheMoney-300);
                moneynessMap.put(Constants.OTM2,atTheMoney-200);
                moneynessMap.put(Constants.OTM1,atTheMoney-100);
                moneynessMap.put(Constants.ATM,atTheMoney);
                moneynessMap.put(Constants.ITM1,atTheMoney+100);
                moneynessMap.put(Constants.ITM2,atTheMoney+200);
                moneynessMap.put(Constants.ITM3,atTheMoney+300);
                moneynessMap.put(Constants.ITM4,atTheMoney+400);
                moneynessMap.put(Constants.ITM5,atTheMoney+500);
            }

            return moneynessMap;

        }catch (KiteException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        } catch (JSONException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        } catch(Exception e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return moneynessMap;
    }

    public Instrument getMoneynessInstrument(String MoneynessStatus, Map<String, Long> moneynessMap, Map<String, Instrument> bankNiftyOptionsMap, String PECE_IND){

        logger.info("-------------- Inside getMoneynessInstrument ---------------------");

        try{

            for(Map.Entry<String,Instrument> entry : bankNiftyOptionsMap.entrySet()){
                //if(!(entry.getValue().getTradingsymbol().equals(""))){
                    Instrument bankNiftyOptInstr = entry.getValue();
                    if(bankNiftyOptInstr.getStrike().equals(Double.toString(moneynessMap.get(MoneynessStatus))) && bankNiftyOptInstr.instrument_type.equals(PECE_IND)){
                        return entry.getValue();
                    }
                }
            //}

        }catch(Exception e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return null;
    }



    public Order placeBuyOrder(KiteConnect kiteConnect, int qty, Instrument instrToTrade, String buyOrSell, double price){

        logger.info("-------------- Inside placeBuyOrder ---------------------");

        Order order = null;
        try{
            OrderParams orderParams = new OrderParams();
            orderParams.quantity = qty;
            orderParams.orderType = Constants.ORDER_TYPE_LIMIT;
            orderParams.tradingsymbol = instrToTrade.tradingsymbol;
            orderParams.product = Constants.PRODUCT_NRML;
            orderParams.exchange = Constants.EXCHANGE_NFO;
            orderParams.transactionType = buyOrSell;
            orderParams.validity = Constants.VALIDITY_DAY;
            orderParams.price = price;
            orderParams.triggerPrice = 0.0;
            orderParams.tag = "myTag"; //tag is optional and it cannot be more than 8 characters and only alphanumeric is allowed

            order = kiteConnect.placeOrder(orderParams, Constants.VARIETY_REGULAR);
            waitForOrderToBeFilled(order, instrToTrade);
            logger.info("OrderId of " + instrToTrade.tradingsymbol + " is >> " + order.orderId);
            return order;

        }catch(KiteException e){
            e.printStackTrace();
            logger.log(Level.ERROR, e.getMessage(), e);
        }catch(IOException e){
            e.printStackTrace();
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return order;
    }

    public Order placeSellOrder(KiteConnect kiteConnect, Order parentBuyOrder, int qty, Instrument instrToTrade, String buyOrSell, double price){

        logger.info("-------------- Inside placeSellOrder ---------------------");

        Order order = null;
        try{
            OrderParams orderParams = new OrderParams();
            orderParams.quantity = qty;
            orderParams.orderType = Constants.ORDER_TYPE_LIMIT;
            orderParams.tradingsymbol = instrToTrade.tradingsymbol;
            orderParams.product = Constants.PRODUCT_NRML;
            orderParams.exchange = Constants.EXCHANGE_NFO;
            orderParams.transactionType = buyOrSell;
            orderParams.validity = Constants.VALIDITY_DAY;
            orderParams.price = price;
            orderParams.triggerPrice = 0.0;
            orderParams.tag = "myTag"; //tag is optional and it cannot be more than 8 characters and only alphanumeric is allowed

            Order modifiedOrder = kiteConnect.modifyOrder(parentBuyOrder.orderId, orderParams, Constants.VARIETY_REGULAR);
            System.out.println("modifiedOrder returned from  placeSellOrder >> " + modifiedOrder.orderId);
            waitForSquareOff(modifiedOrder, instrToTrade);
            return modifiedOrder;

        }catch(KiteException e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }catch(IOException e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return order;
    }


    public void waitForOrderToBeFilled(Order order, Instrument instrToTrade){

        logger.info("-------------- Inside waitForOrderToBeFilled ---------------------");

        boolean isOrderPending = true;
        try{

            /*List<Order> orderHist = kiteConnect.getOrderHistory(order.orderId);
            for(Order stage : orderHist){

                if(stage.filledQuantity.equalsIgnoreCase(Integer.toString(Constants.QUANTITY)) && stage.status.equalsIgnoreCase(Constants.ORDER_COMPLETED)){
                    logger.info("Order was successfully placed for Quantity >> " + Constants.QUANTITY);
                    isOrderPending = false;
                    break;
                }else{
                    logger.info("Unfilled Order. Placed for >> " + Constants.QUANTITY + ".Filled Qty : " + stage.filledQuantity + ". Status of current stage is >> " + stage.status);
                }
            }

            if(isOrderPending){
                Thread.sleep(2000);
                waitForOrderToBeFilled(order);
            }*/


            while(isOrderPending){

                Map<String, List<Position>> posList = exampleImpl.getPositions(kiteConnect);
                int noOfPos = posList.get("net").size();
                for(int i=0; i< noOfPos; i++){
                    if(posList.get("net").get(i).tradingSymbol.equalsIgnoreCase(instrToTrade.tradingsymbol)){
                        if(posList.get("net").get(i).netQuantity != Constants.QUANTITY){

                            isOrderPending=true;
                            int qty = posList.get("net").get(i).netQuantity;
                            String sym = posList.get("net").get(i).tradingSymbol;
                            logger.info("net Quantity filled for Trading Symbol >> " + sym + " is >> " + qty);

                        }else{

                            logger.info(" ********************** Square off completed ********************** ");

                            logger.info("No of net Positions >> " + posList.get("net").size());

                            int netQty = posList.get("net").get(i).netQuantity;
                            String tradingSymbol = posList.get("net").get(i).tradingSymbol;
                            double buym2m = posList.get("net").get(i).buym2m;
                            double buyPrice = posList.get("net").get(i).buyPrice;
                            double buyQuantity = posList.get("net").get(i).buyQuantity;
                            double buyValue = posList.get("net").get(i).buyValue;
                            double closePrice = posList.get("net").get(i).closePrice;
                            double dayBuyPrice = posList.get("net").get(i).dayBuyPrice;
                            double dayBuyQuantity = posList.get("net").get(i).dayBuyQuantity;
                            double dayBuyValue = posList.get("net").get(i).dayBuyValue;
                            double daySellPrice = posList.get("net").get(i).daySellPrice;
                            double daySellQuantity = posList.get("net").get(i).daySellQuantity;
                            double daySellValue = posList.get("net").get(i).daySellValue;
                            String  exchange = posList.get("net").get(i).exchange;
                            String instrumentToken = posList.get("net").get(i).instrumentToken;
                            double lastPrice = posList.get("net").get(i).lastPrice;
                            double m2m = posList.get("net").get(i).m2m;
                            double multiplier = posList.get("net").get(i).multiplier;
                            double netValue = posList.get("net").get(i).netValue;
                            double overnightQuantity = posList.get("net").get(i).overnightQuantity;
                            double pnl = posList.get("net").get(i).pnl;
                            String product = posList.get("net").get(i).product;
                            double realised = posList.get("net").get(i).realised;
                            double sellm2m = posList.get("net").get(i).sellm2m;
                            double sellPrice = posList.get("net").get(i).sellPrice;
                            double sellQuantity = posList.get("net").get(i).sellQuantity;
                            double sellValue = posList.get("net").get(i).sellValue;
                            double unrealised = posList.get("net").get(i).unrealised;
                            double value = posList.get("net").get(i).value;

                            logger.info("netQty >> " + netQty + " tradingSymbol >> " + tradingSymbol + " buym2m >> " + buym2m + " buyPrice>> " +
                                    buyPrice + " buyQuantity >> " + buyQuantity + " buyValue >> " + buyValue + " closePrice>> " + closePrice +
                                    " dayBuyPrice >> " + dayBuyPrice + " dayBuyQuantity >> "  + dayBuyQuantity + " dayBuyValue >> "  + dayBuyValue +
                                    " daySellPrice >> "  + daySellPrice + " daySellQuantity >> " + daySellQuantity + " daySellValue >> "  + daySellValue +
                                    " exchange >> "  + exchange + " instrumentToken >> "  +  instrumentToken + " lastPrice >> "  + lastPrice + " m2m >> "  +
                                    m2m + " multiplier >> "  + multiplier + " netValue >> " + netValue + " overnightQuantity >> " + overnightQuantity +
                                    " pnl >> " + pnl + " >> " + " product >> " + product + " realised >> "  + realised + " sellm2m >> "  + sellm2m +
                                    " sellPrice >> "  + sellPrice + " sellQuantity >> "  + sellQuantity + " sellValue >> " + sellValue + " unrealised >> " +
                                    unrealised + " value >> " + value
                            );
                        }
                    }
                }
            }
        }catch(KiteException e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }catch(IOException e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    public void waitForSquareOff(Order order, Instrument instrToTrade){

        logger.info("-------------- Inside waitForSquareOff ---------------------");

        boolean isOrderPending = true;
        try{

            /*List<Order> orderHist = kiteConnect.getOrderHistory(order.orderId);
            for(Order stage : orderHist){
                if(stage.status.equalsIgnoreCase(Constants.ORDER_COMPLETED)){
                    logger.info("Square OFF was successful");
                    isOrderPending = false;
                    break;
                }else{
                    logger.info("Square OFF Pending....Placed for >> " + Constants.QUANTITY + ".Filled Qty : " + stage.filledQuantity + ". Status of current stage is >> " + stage.status);
                }
            }

            if(isOrderPending){
                Thread.sleep(2000);
                waitForSquareOff(order);
            }*/

            while(isOrderPending){
                Map<String, List<Position>> posList = exampleImpl.getPositions(kiteConnect);
                int noOfPos = posList.get("net").size();
                for(int i=0; i< noOfPos; i++){
                    if(posList.get("net").get(i).tradingSymbol.equalsIgnoreCase(instrToTrade.tradingsymbol)){
                        if(posList.get("net").get(i).netQuantity != 0){
                            isOrderPending=true;
                            int qty = posList.get("net").get(i).netQuantity;
                            String sym = posList.get("net").get(i).tradingSymbol;
                            logger.info("net Quantity of " + sym + " is >> " + qty);
                        }else{

                            logger.info(" ********************** Square off completed ********************** ");

                            logger.info("No of net Positions >> " + posList.get("net").size());

                            int netQty = posList.get("net").get(i).netQuantity;
                            String tradingSymbol = posList.get("net").get(i).tradingSymbol;
                            double buym2m = posList.get("net").get(i).buym2m;
                            double buyPrice = posList.get("net").get(i).buyPrice;
                            double buyQuantity = posList.get("net").get(i).buyQuantity;
                            double buyValue = posList.get("net").get(i).buyValue;
                            double closePrice = posList.get("net").get(i).closePrice;
                            double dayBuyPrice = posList.get("net").get(i).dayBuyPrice;
                            double dayBuyQuantity = posList.get("net").get(i).dayBuyQuantity;
                            double dayBuyValue = posList.get("net").get(i).dayBuyValue;
                            double daySellPrice = posList.get("net").get(i).daySellPrice;
                            double daySellQuantity = posList.get("net").get(i).daySellQuantity;
                            double daySellValue = posList.get("net").get(i).daySellValue;
                            String  exchange = posList.get("net").get(i).exchange;
                            String instrumentToken = posList.get("net").get(i).instrumentToken;
                            double lastPrice = posList.get("net").get(i).lastPrice;
                            double m2m = posList.get("net").get(i).m2m;
                            double multiplier = posList.get("net").get(i).multiplier;
                            double netValue = posList.get("net").get(i).netValue;
                            double overnightQuantity = posList.get("net").get(i).overnightQuantity;
                            double pnl = posList.get("net").get(i).pnl;
                            String product = posList.get("net").get(i).product;
                            double realised = posList.get("net").get(i).realised;
                            double sellm2m = posList.get("net").get(i).sellm2m;
                            double sellPrice = posList.get("net").get(i).sellPrice;
                            double sellQuantity = posList.get("net").get(i).sellQuantity;
                            double sellValue = posList.get("net").get(i).sellValue;
                            double unrealised = posList.get("net").get(i).unrealised;
                            double value = posList.get("net").get(i).value;

                            logger.info("netQty >> " + netQty + " tradingSymbol >> " + tradingSymbol + " buym2m >> " + buym2m + " buyPrice>> " +
                            buyPrice + " buyQuantity >> " + buyQuantity + " buyValue >> " + buyValue + " closePrice>> " + closePrice +
                            " dayBuyPrice >> " + dayBuyPrice + " dayBuyQuantity >> "  + dayBuyQuantity + " dayBuyValue >> "  + dayBuyValue +
                            " daySellPrice >> "  + daySellPrice + " daySellQuantity >> " + daySellQuantity + " daySellValue >> "  + daySellValue +
                            " exchange >> "  + exchange + " instrumentToken >> "  +  instrumentToken + " lastPrice >> "  + lastPrice + " m2m >> "  +
                             m2m + " multiplier >> "  + multiplier + " netValue >> " + netValue + " overnightQuantity >> " + overnightQuantity +
                            " pnl >> " + pnl + " >> " + " product >> " + product + " realised >> "  + realised + " sellm2m >> "  + sellm2m +
                            " sellPrice >> "  + sellPrice + " sellQuantity >> "  + sellQuantity + " sellValue >> " + sellValue + " unrealised >> " +
                            unrealised + " value >> " + value
                            );
                        }
                    }
                }
            }

        }catch(KiteException e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }catch(IOException e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    public String padSpace(String str){

        int cuttLenth = 0;
        int spaceToAdd = 0 ;
        StringBuffer padSpace = new StringBuffer();

        if(str.length() != 30 ){
            int currLength = str.length();
            spaceToAdd = 30 - currLength;
        }

        for(int i=1; i<=spaceToAdd; i++){
            padSpace = padSpace.append(" ");
        }
        return padSpace.toString();

    }

    public void printQuoteContent(Instrument instrument) throws KiteException, IOException {

        try {
            Map<String, Quote> AtmCEQuoteBuyLevel = exampleImpl.getQuoteForSingleInstrument(kiteConnect, instrument);

            Quote quote = AtmCEQuoteBuyLevel.get(Long.toString(instrument.getInstrument_token()));
            double volumeTradedToday = quote.volumeTradedToday;
            double lastTradedQuantity = quote.lastTradedQuantity;
            Date lastTradedTime = quote.lastTradedTime;
            double net_change = quote.change;
            double oi = quote.oi;
            double sellQuantity = quote.sellQuantity;
            double last_price = quote.lastPrice;
            double buy_quantity = quote.buyQuantity;
            double ohlcHigh = quote.ohlc.high;
            double ohlcLow = quote.ohlc.low;
            double ohlcClose = quote.ohlc.close;
            double ohlcOpen = quote.ohlc.open;
            double averagePrice = quote.averagePrice;
            double oiDayHigh = quote.oiDayHigh;
            double oiDayLow = quote.oiDayLow;
            int depthBuyQuantity = quote.depth.buy.get(0).getQuantity();
            double depthBuyPrice = quote.depth.buy.get(0).getPrice();
            int depthBuyOrders = quote.depth.buy.get(0).getOrders();
            int depthSellQuantity = quote.depth.sell.get(0).getQuantity();
            double depthSellPrice = quote.depth.sell.get(0).getPrice();
            int depthSellOrders = quote.depth.sell.get(0).getOrders();

            logger.info(instrument.getTradingsymbol() + " >> last_price >> " + last_price + " AvgPrice >> " + averagePrice + " LTQty >> " + lastTradedQuantity + " net_change >> "
                    + net_change + " BuyQty >> " + buy_quantity + " SellQty >> " + sellQuantity + " ohlcO >> " + ohlcOpen + " ohlcH >> " + ohlcHigh +
                    " ohlcL >> " + ohlcLow + " ohlcC >> " + ohlcClose + " OI >> " + oi + " depthBuyOrders >> " + depthBuyOrders + " depthBuyQty >> "
                    + depthBuyQuantity + " depthBuyPrice >> " + depthBuyPrice + " depthSellOrders >> " + depthSellOrders + " depthSellQty >> " +
                    depthSellQuantity + " depthSellPrice >> " + depthSellPrice + " volumeTradedToday >> " + volumeTradedToday + " LTTime >> " +
                    lastTradedTime + " oiDayHigh >> " + oiDayHigh + " oiDayLow >> " + oiDayLow);

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    public String findCurrentTrend(){

        logger.info("-------------- Inside findCurrentTrend ---------------------");

        String currentTrend = "";

        try{

            int noOfRise = 0;
            int noOfFall = 0;
            int nochange = 0;

            logger.info("No of Price to be analyzed : " + lastMinPriceList.size());

            for(int i=0; i<lastMinPriceList.size()-1; i++){
                if(lastMinPriceList.get(i).doubleValue() > lastMinPriceList.get(i+1).doubleValue())
                    ++noOfFall;
                else if(lastMinPriceList.get(i).doubleValue() < lastMinPriceList.get(i+1).doubleValue())
                    ++noOfRise;
                else if(lastMinPriceList.get(i).doubleValue() == lastMinPriceList.get(i+1).doubleValue())
                    ++nochange;
            }

            double priceRangeDiff = lastMinPriceList.get(lastMinPriceList.size()-1) - lastMinPriceList.get(0);
            double highLowDiff = lastOhlc.getHigh()-lastOhlc.getLow();

            double divisor = lastMinPriceList.size()-1;

            double fallPercentage = (noOfFall/divisor)*100;
            double flatPercentage = (nochange/divisor)*100;
            double risePercentage = (noOfRise/divisor)*100;

            logger.info("fallPercentage >> " + fallPercentage);
            logger.info("risePercentage >> " + risePercentage);
            logger.info("flatPercentage >> " + flatPercentage);

            if((fallPercentage > 60)){
                logger.info("highLowDiff >> " + highLowDiff);
                logger.info("Analysis indicates a Bearish trend. lastMinPriceList >> " + lastMinPriceList);
                logger.info("Price range diff for Bearish Trend >> " + priceRangeDiff);
                currentTrend = Constants.BEARISH;
            }
            else if((risePercentage > 60)){
                logger.info("highLowDiff >> " + highLowDiff);
                logger.info("Analysis indicates a bullish trend. lastMinPriceList >> " + lastMinPriceList);
                logger.info("Price range diff for Bullish Trend >> " + priceRangeDiff);
                currentTrend = Constants.BULLISH;
            }
            else{
                logger.info("highLowDiff >> " + highLowDiff);
                logger.info("Analysis indicates a Flat trend. lastMinPriceList >> " + lastMinPriceList);
                logger.info("Price range diff for Flat trend >> " + priceRangeDiff);
                currentTrend = Constants.FLAT;
            }


        }catch(Exception e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return currentTrend;
    }

    public void catchRallyIfAny(Instrument atmCEInstrument, double sellPrice){

        double prevSellPrice = 1000;
        double currSellPrice = 1000;
        boolean isBullish = true ;

        try{

            logger.info("-------------- Inside Catch Rally ---------------------");

            Map<String, Quote> atmCEBuyerQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, atmCEInstrument);
            double atmCEBuyerPriceInMkt =  atmCEBuyerQuote.get(Long.toString(atmCEInstrument.getInstrument_token())).depth.buy.get(0).getPrice();

            isBullish = atmCEBuyerPriceInMkt >= sellPrice;

            /* Detect a rally */
            while(isBullish){

                atmCEBuyerQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, atmCEInstrument);
                prevSellPrice = atmCEBuyerQuote.get(Long.toString(atmCEInstrument.getInstrument_token())).depth.buy.get(0).getPrice();
                logger.info("Previous Sell Price >> " + prevSellPrice);

                Thread.sleep(1000);

                atmCEBuyerQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, atmCEInstrument);
                currSellPrice =  atmCEBuyerQuote.get(Long.toString(atmCEInstrument.getInstrument_token())).depth.buy.get(0).getPrice();
                logger.info("Current Sell Price >> " + currSellPrice);

                if(currSellPrice >= prevSellPrice)
                    isBullish = true;
                else{
                    isBullish = false;
                    logger.info("Missed a min profit Margin Opportunity");
                }
            }
            logger.info("Exit Catch Rally");
        }catch(KiteException e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }catch(IOException e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }catch(InterruptedException e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }

    }

    public void printPrice(Instrument atmCEInstrument, double price){
        logger.info(atmCEInstrument.getTradingsymbol() + " is trading @ " + price);
    }

    public String isPriceMovementBullish(Instrument instrument, int timePeriodInSeconds){

        String currentTrend = "";

        try{

            Instrument atmCE = getMoneynessInstrument(Constants.ATM, callMoneynessMap, bankNiftyOptionsMap, Constants.CE_INDICATOR);
            Instrument atmPE = getMoneynessInstrument(Constants.ATM, putMoneynessMap, bankNiftyOptionsMap, Constants.PE_INDICATOR);

            List<Double> premDiffList = new ArrayList<Double>();
            double cumulativePremiumDiff = 0;

            boolean marginNotAchieved = true;
            int counter = 0;

            logger.info("Start Analyzing data for last " + timePeriodInSeconds + " seconds for Instrument >> " + instrument.getTradingsymbol());

            while(marginNotAchieved){

                Map<String, Quote> instrumentQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, instrument);
                double instrumentSellPriceInMkt =  instrumentQuote.get(Long.toString(instrument.getInstrument_token())).depth.sell.get(0).getPrice();
                lastMinPriceList.add(instrumentSellPriceInMkt);
                printQuoteContent(bankNiftyFuture);
                printQuoteContent(atmCE);
                printQuoteContent(atmPE);

                //logger.info(instrument.getTradingsymbol() + " is trading @ " + instrumentSellPriceInMkt);

                /*if(i==0)
                    lastOhlc.setOpen(instrumentSellPriceInMkt);
                if(i==29)
                    lastOhlc.setClose(instrumentSellPriceInMkt);*/
                if(counter !=0){
                    double premDiff = lastMinPriceList.get(counter) - lastMinPriceList.get(counter-1);
                    cumulativePremiumDiff = cumulativePremiumDiff + premDiff;
                }

                /*cumulativePremiumDiff=0;
                for(int k=0; k<premDiffList.size();k++){
                    cumulativePremiumDiff = cumulativePremiumDiff + premDiffList.get(k);
                }*/

                if(cumulativePremiumDiff > 3){
                    marginNotAchieved=false;
                    logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~ Cumulative premium difference is >> "+ cumulativePremiumDiff +". Premium of " + instrument.tradingsymbol + " is rising ~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");
                    currentTrend = Constants.BULLISH;
                    break;
                }else if(cumulativePremiumDiff < -3){
                    marginNotAchieved=false;
                    logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~ Cumulative premium difference is >> "+ cumulativePremiumDiff + ". Premium of " + instrument.tradingsymbol + " is Falling ~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");
                    currentTrend = Constants.BEARISH;
                    break;
                }

                Thread.sleep(500);
                counter=counter+1;

                if(counter > 30){
                    logger.info("cumulativePremiumDiff >> " + cumulativePremiumDiff);
                    logger.info("Finished Analyzing data for last " + timePeriodInSeconds + " seconds for Instrument >> " + instrument.getTradingsymbol());
                    logger.info("30 counts completed .....looping afresh...");
                    logger.info("Prices collected in this loop : " + lastMinPriceList);
                    counter = 0;
                    lastMinPriceList.clear();
                }
            }

            /* Finds high & low price from OHLCData lastOhlc & lastMinPriceList*/

           /* double smallest = lastMinPriceList.get(0);
            double largest = lastMinPriceList.get(0);

            for(int i=1; i< lastMinPriceList.size(); i++)
            {
                if(lastMinPriceList.get(i) > largest)
                    largest = lastMinPriceList.get(i);
                else if (lastMinPriceList.get(i) < smallest)
                    smallest = lastMinPriceList.get(i);
            }

            lastOhlc.setHigh(largest);
            lastOhlc.setLow(smallest);*/



        }catch (KiteException e) {
            logger.error(e.message+" "+e.code+" "+e.getClass().getName());
            logger.log(Level.ERROR, e.getMessage(), e);
        } catch (JSONException e) {
            logger.error(e.getMessage()+" "+" "+e.getClass().getName());
            logger.log(Level.ERROR, e.getMessage(), e);
        }catch (IOException e) {
            logger.error(e.getMessage()+" "+" "+e.getClass().getName());
            logger.log(Level.ERROR, e.getMessage(), e);
        } catch(Exception e){
            logger.error(e.getMessage()+" "+" "+e.getClass().getName());
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return currentTrend;
    }

    public  double findMean(OHLCData ohlcData){

        double mean = 0;

        try{
            mean = Math.abs((ohlcData.getHigh() + ohlcData.getLow())/2);
            logger.info("Mean price of last min : >> " + mean);
        }catch(Exception e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return mean;
    }

    /* Finds high & low price from OHLCData lastOhlc & lastMinPriceList*//*
    public void findHighLow() {

        try{
            //assign first element of an array to largest and smallest
            double smallest = lastMinPriceList.get(0);
            double largest = lastMinPriceList.get(0);

            for(int i=1; i< lastMinPriceList.size(); i++)
            {
                if(lastMinPriceList.get(i) > largest)
                    largest = lastMinPriceList.get(i);
                else if (lastMinPriceList.get(i) < smallest)
                    smallest = lastMinPriceList.get(i);
            }

            lastOhlc.setHigh(largest);
            lastOhlc.setLow(smallest);

        }catch(Exception e){
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }*/
}