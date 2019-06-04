package com.trading.application;

import com.neovisionaries.ws.client.WebSocketException;
import com.trading.application.utils.OHLCData;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.*;
import com.zerodhatech.ticker.*;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sujith on 15/10/16.
 */
public class ExampleImpl {

    Logger logger = Logger.getLogger(ExampleImpl.class.getName());


    public void getProfile(KiteConnect kiteConnect) throws IOException, KiteException {
        Profile profile = kiteConnect.getProfile();
        logger.info(profile.userName);
    }

    /**Gets Margin.*/
    public void getMargins(KiteConnect kiteConnect) throws KiteException, IOException {
        // Get margins returns margin model, you can pass equity or commodity as arguments to get margins of respective segments.
        //Margins margins = kiteConnect.getMargins("equity");
        Margin margins = kiteConnect.getMargins("equity");
        logger.info(margins.available.cash);
        logger.info(margins.utilised.debits);
        logger.info(margins.utilised.m2mUnrealised);
    }

    /**Place order.*/
    public String placeOrder(KiteConnect kiteConnect) throws KiteException, IOException {
        /** Place order method requires a orderParams argument which contains,
         * tradingsymbol, exchange, transaction_type, order_type, quantity, product, price, trigger_price, disclosed_quantity, validity
         * squareoff_value, stoploss_value, trailing_stoploss
         * and variety (value can be regular, bo, co, amo)
         * place order will return order model which will have only orderId in the order model
         *
         * Following is an example param for LIMIT order,
         * if a call fails then KiteException will have error message in it
         * Success of this call implies only order has been placed successfully, not order execution. */

        OrderParams orderParams = new OrderParams();
        orderParams.quantity = 1;
        orderParams.orderType = Constants.ORDER_TYPE_LIMIT;
        orderParams.tradingsymbol = "ASHOKLEY";
        orderParams.product = Constants.PRODUCT_CNC;
        orderParams.exchange = Constants.EXCHANGE_NSE;
        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY;
        orderParams.validity = Constants.VALIDITY_DAY;
        orderParams.price = 122.2;
        orderParams.triggerPrice = 0.0;
        orderParams.tag = "myTag"; //tag is optional and it cannot be more than 8 characters and only alphanumeric is allowed

        Order order = kiteConnect.placeOrder(orderParams, Constants.VARIETY_REGULAR);
        logger.info(order.orderId);
        return order.orderId;
    }

    /** Place bracket order.*/
    public void placeBracketOrder(KiteConnect kiteConnect) throws KiteException, IOException {
        /** Bracket order:- following is example param for bracket order*
         * trailing_stoploss and stoploss_value are points and not tick or price
         */
        OrderParams orderParams = new OrderParams();
        orderParams.quantity = 1;
        orderParams.orderType = Constants.ORDER_TYPE_LIMIT;
        orderParams.price = 800.0;
        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY;
        orderParams.tradingsymbol = "BANKNIFTY 23rd MAY 28900 PE";
        orderParams.trailingStoploss = 1.0;
        orderParams.stoploss = 2.0;
        orderParams.exchange = Constants.EXCHANGE_NSE;
        orderParams.validity = Constants.VALIDITY_DAY;
        orderParams.squareoff = 3.0;
        orderParams.product = Constants.PRODUCT_MIS;
        Order order10 = kiteConnect.placeOrder(orderParams, Constants.VARIETY_BO);
        logger.info(order10.orderId);
    }

    /** Place cover order.*/
    public void placeCoverOrder(KiteConnect kiteConnect) throws KiteException, IOException {
        /** Cover Order:- following is an example param for the cover order
         * key: quantity value: 1
         * key: price value: 0
         * key: transaction_type value: BUY
         * key: tradingsymbol value: HINDALCO
         * key: exchange value: NSE
         * key: validity value: DAY
         * key: trigger_price value: 157
         * key: order_type value: MARKET
         * key: variety value: co
         * key: product value: MIS
         */
        OrderParams orderParams = new OrderParams();
        orderParams.price = 0.0;
        orderParams.quantity = 1;
        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY;
        orderParams.orderType = Constants.ORDER_TYPE_MARKET;
        orderParams.tradingsymbol = "SOUTHBANK";
        orderParams.exchange = Constants.EXCHANGE_NSE;
        orderParams.validity = Constants.VALIDITY_DAY;
        orderParams.triggerPrice = 30.5;
        orderParams.product = Constants.PRODUCT_MIS;

        Order order11 = kiteConnect.placeOrder(orderParams, Constants.VARIETY_CO);
        logger.info(order11.orderId);
    }

    /** Get trigger range.*/
    public void getTriggerRange(KiteConnect kiteConnect, String[] instrument_exch_token) throws KiteException, IOException {
        // You need to send transaction_type, exchange and tradingsymbol to get trigger range.
        //String[] instruments = {"BSE:INFY", "NSE:APOLLOTYRE", "NSE:SBIN"};
        Map<String, TriggerRange> triggerRangeMap = kiteConnect.getTriggerRange(instrument_exch_token, Constants.TRANSACTION_TYPE_BUY);
        logger.info(triggerRangeMap.get(instrument_exch_token).lower);
        logger.info(triggerRangeMap.get(instrument_exch_token).upper);
        logger.info(triggerRangeMap.get(instrument_exch_token).percentage);
    }

    /** Get orderbook.*/
    public void getOrders(KiteConnect kiteConnect) throws KiteException, IOException {
        // Get orders returns order model which will have list of orders inside, which can be accessed as follows,
        List<Order> orders = kiteConnect.getOrders();
        for(int i = 0; i< orders.size(); i++){
            logger.info(orders.get(i).tradingSymbol+" "+orders.get(i).orderId+" "+orders.get(i).parentOrderId+" "+orders.get(i).orderType+" "+orders.get(i).averagePrice+" "+orders.get(i).exchangeTimestamp);
        }
        logger.info("list of orders size is "+orders.size());
    }

    /** Get order details*/
    public void getOrder(KiteConnect kiteConnect) throws KiteException, IOException {
        List<Order> orders = kiteConnect.getOrderHistory("180111000561605");
        for(int i = 0; i< orders.size(); i++){
            logger.info(orders.get(i).orderId+" "+orders.get(i).status);
        }
        logger.info("list size is "+orders.size());
    }

    /** Get tradebook*/
    public void getTrades(KiteConnect kiteConnect) throws KiteException, IOException {
        // Returns tradebook.
        List<Trade> trades = kiteConnect.getTrades();
        for (int i=0; i < trades.size(); i++) {
            logger.info(trades.get(i).tradingSymbol+" "+trades.size());
        }
        logger.info(trades.size());
    }

    /** Get trades for an order.*/
    public void getTradesWithOrderId(KiteConnect kiteConnect) throws KiteException, IOException {
        // Returns trades for the given order.
        List<Trade> trades = kiteConnect.getOrderTrades("180111000561605");
        logger.info(trades.size());
    }

    /** Modify order.*/
    public void modifyOrder(KiteConnect kiteConnect, String orderId) throws KiteException, IOException {
        // Order modify request will return order model which will contain only order_id.
        OrderParams orderParams =  new OrderParams();
        orderParams.quantity = 1;
        orderParams.orderType = Constants.ORDER_TYPE_LIMIT;
        orderParams.tradingsymbol = "ASHOKLEY";
        orderParams.product = Constants.PRODUCT_CNC;
        orderParams.exchange = Constants.EXCHANGE_NSE;
        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY;
        orderParams.validity = Constants.VALIDITY_DAY;
        orderParams.price = 122.25;

        Order order21 = kiteConnect.modifyOrder("orderId", orderParams, Constants.VARIETY_REGULAR);
        logger.info(order21.orderId);
    }

    /** Modify first leg bracket order.*/
    public void modifyFirstLegBo(KiteConnect kiteConnect) throws KiteException, IOException {
        OrderParams orderParams = new OrderParams();
        orderParams.quantity = 1;
        orderParams.price = 31.0;
        orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY;
        orderParams.tradingsymbol = "SOUTHBANK";
        orderParams.exchange = Constants.EXCHANGE_NSE;
        orderParams.validity = Constants.VALIDITY_DAY;
        orderParams.product = Constants.PRODUCT_MIS;
        orderParams.tag = "myTag";
        orderParams.triggerPrice = 0.0;

        Order order = kiteConnect.modifyOrder("180116000798058", orderParams, Constants.VARIETY_BO);
        logger.info(order.orderId);
    }

    public void modifySecondLegBoSLM(KiteConnect kiteConnect) throws KiteException, IOException {

        OrderParams orderParams = new OrderParams();
        orderParams.parentOrderId = "180116000798058";
        orderParams.tradingsymbol = "SOUTHBANK";
        orderParams.exchange = Constants.EXCHANGE_NSE;
        orderParams.product = Constants.PRODUCT_MIS;
        orderParams.validity = Constants.VALIDITY_DAY;
        orderParams.triggerPrice = 30.5;
        orderParams.price = 0.0;
        orderParams.orderType = Constants.ORDER_TYPE_SLM;
        orderParams.transactionType = Constants.TRANSACTION_TYPE_SELL;

        Order order = kiteConnect.modifyOrder("180116000812154", orderParams, Constants.VARIETY_BO);
        logger.info(order.orderId);
    }

    public void modifySecondLegBoLIMIT(KiteConnect kiteConnect) throws KiteException, IOException {
        OrderParams orderParams =  new OrderParams();
        orderParams.parentOrderId = "180116000798058";
        orderParams.tradingsymbol = "SOUTHBANK";
        orderParams.exchange = Constants.EXCHANGE_NSE;
        orderParams.quantity =  1;
        orderParams.product = Constants.PRODUCT_MIS;
        orderParams.validity = Constants.VALIDITY_DAY;
        orderParams.price = 35.3;
        orderParams.orderType = Constants.ORDER_TYPE_LIMIT;
        orderParams.transactionType = Constants.TRANSACTION_TYPE_SELL;

        Order order = kiteConnect.modifyOrder("180116000812153", orderParams, Constants.VARIETY_BO);
        logger.info(order.orderId);
    }

    /** Cancel an order*/
    public void cancelOrder(KiteConnect kiteConnect, String orderId) throws KiteException, IOException {
        // Order modify request will return order model which will contain only order_id.
        // Cancel order will return order model which will only have orderId.
        Order order2 = kiteConnect.cancelOrder(orderId, Constants.VARIETY_REGULAR);
        logger.info("Cancelled Order ID >> " + order2.orderId);
    }

    public void exitBracketOrder(KiteConnect kiteConnect) throws KiteException, IOException {
        Order order = kiteConnect.cancelOrder("180116000812153","180116000798058", Constants.VARIETY_BO);
        logger.info(order.orderId);
    }

    /** Get all positions.*/
    public Map<String, List<Position>> getPositions(KiteConnect kiteConnect) throws KiteException, IOException {
        // Get positions returns position model which contains list of positions.
        Map<String, List<Position>> position = kiteConnect.getPositions();
        /*logger.info(position.get("net").size());
        logger.info(position.get("day").size());*/
        return position;
    }

    /** Get holdings.*/
    public void getHoldings(KiteConnect kiteConnect) throws KiteException, IOException {
        // Get holdings returns holdings model which contains list of holdings.
        List<Holding> holdings = kiteConnect.getHoldings();
        logger.info(holdings.size());
    }

    /** Converts position*/
    public void converPosition(KiteConnect kiteConnect) throws KiteException, IOException {
        //Modify product can be used to change MIS to NRML(CNC) or NRML(CNC) to MIS.
        JSONObject jsonObject6 = kiteConnect.convertPosition("ASHOKLEY", Constants.EXCHANGE_NSE, Constants.TRANSACTION_TYPE_BUY, Constants.POSITION_DAY, Constants.PRODUCT_MIS, Constants.PRODUCT_CNC, 1);
        logger.info(jsonObject6);
    }

    /** Get all instruments that can be traded using kite connect.*/
    public List<Instrument> getAllInstruments(KiteConnect kiteConnect) throws KiteException, IOException {
        // Get all instruments list. This call is very expensive as it involves downloading of large data dump.
        // Hence, it is recommended that this call be made once and the results stored locally once every morning before market opening.
        List<Instrument> instruments = kiteConnect.getInstruments();
        logger.info(instruments.size());
        return instruments;
    }

    /** Get instruments for the desired exchange.*/
    public List<Instrument> getInstrumentsForExchange(KiteConnect kiteConnect, String exchange) throws KiteException, IOException {
        // Get instruments for an exchange.
        List<Instrument> instruments = kiteConnect.getInstruments(exchange);
        logger.info(instruments.size());
        return instruments;
    }

    /** Get quote for a scrip.*/
    public void getQuote(KiteConnect kiteConnect,String[] instrument_exch_token) throws KiteException, IOException {
        // Get quotes returns quote for desired tradingsymbol.
        //String[] instruments = {"256265","BSE:INFY", "NSE:APOLLOTYRE", "NSE:NIFTY 50"};
        Map<String, Quote> quotes = kiteConnect.getQuote(instrument_exch_token);

        logger.info("instrumentToken >> " + quotes.get(instrument_exch_token).instrumentToken+"");
        logger.info("oi >> " + quotes.get(instrument_exch_token).oi +"");
        logger.info("volumeTradedToday >> " + quotes.get(instrument_exch_token).volumeTradedToday+"");
        logger.info("lastTradedQuantity >> " + quotes.get(instrument_exch_token).lastTradedQuantity+"");
        logger.info("lastTradedTime >> " + quotes.get(instrument_exch_token).lastTradedTime+"");
        logger.info("change >> " + quotes.get(instrument_exch_token).change+"");
        logger.info("sellQuantity >> " + quotes.get(instrument_exch_token).sellQuantity+"");
        logger.info("lastPrice >> " + quotes.get(instrument_exch_token).lastPrice+"");
        logger.info("buyQuantity >> " + quotes.get(instrument_exch_token).buyQuantity+"");
        logger.info("ohlc >> " + quotes.get(instrument_exch_token).ohlc+"");
        logger.info("timestamp >> " + quotes.get(instrument_exch_token).timestamp+"");
        logger.info("averagePrice >> " + quotes.get(instrument_exch_token).averagePrice+"");
        logger.info("oiDayHigh >> " + quotes.get(instrument_exch_token).oiDayHigh+"");
        logger.info("oiDayLow >> " + quotes.get(instrument_exch_token).oiDayLow+"");
        logger.info("depth.buy.get(4).getPrice() >> " + quotes.get(instrument_exch_token).depth.buy.get(4).getPrice());
        logger.info("depth.buy.get(4).getQuantity() >> " + quotes.get(instrument_exch_token).depth.buy.get(4).getQuantity());
        logger.info("depth.buy.get(4).getOrders() >> " + quotes.get(instrument_exch_token).depth.buy.get(4).getOrders());
        logger.info("depth.sell.get(4).getPrice() >> " + quotes.get(instrument_exch_token).depth.sell.get(4).getPrice());
        logger.info("depth.sell.get(4).getQuantity() >> " + quotes.get(instrument_exch_token).depth.sell.get(4).getQuantity());
        logger.info("depth.sell.get(4).getOrders() >> " + quotes.get(instrument_exch_token).depth.sell.get(4).getOrders());

        /*logger.info(quotes.get("NSE:APOLLOTYRE").instrumentToken+"");
        logger.info(quotes.get("NSE:APOLLOTYRE").oi +"");
        logger.info(quotes.get("NSE:APOLLOTYRE").depth.buy.get(4).getPrice());
        logger.info(quotes.get("NSE:APOLLOTYRE").timestamp);*/
    }

    public Map<String, Quote> getQuoteForSingleInstrument(KiteConnect kiteConnect,Instrument instrument) throws KiteException, IOException{

        Map<String, Quote> quotes = null;

        try{
            String[] instrumentToken = {Long.toString(instrument.getInstrument_token())};
            quotes = kiteConnect.getQuote(instrumentToken);
            /*logger.info("0thLevelBuyerPrice of " + instrument.getTradingsymbol() + ">> " + quotes.get(Long.toString(instrument.getInstrument_token())).depth.buy.get(0).getPrice());
            logger.info("1thLevelBuyerPrice of " + instrument.getTradingsymbol() + ">> " + quotes.get(Long.toString(instrument.getInstrument_token())).depth.buy.get(1).getPrice());
            logger.info("2thLevelBuyerPrice of " + instrument.getTradingsymbol() + ">> " +  quotes.get(Long.toString(instrument.getInstrument_token())).depth.buy.get(2).getPrice());
            logger.info("3thLevelBuyerPrice of " + instrument.getTradingsymbol() + ">> " + quotes.get(Long.toString(instrument.getInstrument_token())).depth.buy.get(3).getPrice());
            logger.info("4thLevelBuyerPrice of " + instrument.getTradingsymbol() + ">> " + quotes.get(Long.toString(instrument.getInstrument_token())).depth.buy.get(4).getPrice());*/

            return quotes;

        }catch(Exception e){
            e.printStackTrace();
        }

        return quotes;
    }

    /* Get ohlc and lastprice for multiple instruments at once.
     * Users can either pass exchange with tradingsymbol or instrument token only. For example {NSE:NIFTY 50, BSE:SENSEX} or {256265, 265}*/
    public OHLCData getOHLC(KiteConnect kiteConnect, String[] instrument_exch_token) throws KiteException, IOException {
        //String[] instruments = {"256265","BSE:INFY", "NSE:INFY", "NSE:NIFTY 50"};

        double open = kiteConnect.getOHLC(instrument_exch_token).get("instrument_exch_token").ohlc.open;
        double high = kiteConnect.getOHLC(instrument_exch_token).get("instrument_exch_token").ohlc.high;
        double low = kiteConnect.getOHLC(instrument_exch_token).get("instrument_exch_token").ohlc.low;
        double close = kiteConnect.getOHLC(instrument_exch_token).get("instrument_exch_token").ohlc.close;

        OHLCData ohlc = new OHLCData();
        ohlc.setOpen(open);
        ohlc.setHigh(high);
        ohlc.setLow(low);
        ohlc.setClose(close);

        return ohlc;

    }

    /** Get last price for multiple instruments at once.
     * USers can either pass exchange with tradingsymbol or instrument token only. For example {NSE:NIFTY 50, BSE:SENSEX} or {256265, 265}*/
    public Map<String, Double> getLTPOfMultipleInstrument(KiteConnect kiteConnect, String[] instrument_exch_token,  Map<String, Instrument> bankNiftyOptionsMap) throws KiteException, IOException {
        //String[] instruments = {"256265","BSE:INFY", "NSE:INFY", "NSE:NIFTY 50"};

        Map<String, Double> instrTokenLtpMap = new HashMap<String, Double>();
        List<String> listOfTokens = Arrays.asList(instrument_exch_token);
        for(String token : listOfTokens){
            double ltp = kiteConnect.getLTP(instrument_exch_token).get(instrument_exch_token[0]).lastPrice;
            instrTokenLtpMap.put(token,ltp);
        }
        logger.info("instrTokenLtpMap >> " + instrTokenLtpMap);
        return instrTokenLtpMap;

    }

    public double getLTPOfSingleInstrument(KiteConnect kiteConnect, Instrument instrument) throws KiteException, IOException {
        double ltp = 0;
        try{
            String[] instrumentTokenArr = {Long.toString(instrument.getInstrument_token())};
            ltp = kiteConnect.getLTP(instrumentTokenArr).get(Long.toString(instrument.getInstrument_token())).lastPrice;
            //logger.info("LTP of " + instrument.getTradingsymbol() + " >> " + ltp);
            return ltp;
        }catch(Exception e){
            e.printStackTrace();
        }
        return ltp;
    }

    /** Get historical data for an instrument.*/
    public void getHistoricalData(KiteConnect kiteConnect, long token) throws KiteException, IOException {
        /** Get historical data dump, requires from and to date, intrument token, interval, continuous (for expired F&O contracts)
         * returns historical data object which will have list of historical data inside the object.*/
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date from =  new Date();
        Date to = new Date();
        try {
            from = formatter.parse("2019-05-17 12:00:00");
            to = formatter.parse("2019-05-03 17:49:12");
        }catch (ParseException e) {
            e.printStackTrace();
        }
        HistoricalData historicalData = kiteConnect.getHistoricalData(from, to, new Long(token).toString(), "minute", false);
        logger.info(historicalData.dataArrayList.size());
        logger.info(historicalData.dataArrayList.get(0).volume);
        logger.info(historicalData.dataArrayList.get(historicalData.dataArrayList.size() - 1).volume);
    }

    /** Logout user.*/
    public void logout(KiteConnect kiteConnect) throws KiteException, IOException {
        /** Logout user and kill session. */
        JSONObject jsonObject10 = kiteConnect.logout();
        logger.info(jsonObject10);
    }

    /** Retrieve mf instrument dump */
    public void getMFInstruments(KiteConnect kiteConnect) throws KiteException, IOException {
        List<MFInstrument> mfList = kiteConnect.getMFInstruments();
        logger.info("size of mf instrument list: "+mfList.size());
    }

    /* Get all mutualfunds holdings */
    public void getMFHoldings(KiteConnect kiteConnect) throws KiteException, IOException {
        List<MFHolding> MFHoldings = kiteConnect.getMFHoldings();
        logger.info("mf holdings "+ MFHoldings.size());
    }

    /* Place a mutualfunds order */
    public void placeMFOrder(KiteConnect kiteConnect) throws KiteException, IOException {
        logger.info("place order: "+ kiteConnect.placeMFOrder("INF174K01LS2", Constants.TRANSACTION_TYPE_BUY, 5000, 0, "myTag").orderId);
    }

    /* cancel mutualfunds order */
    public void cancelMFOrder(KiteConnect kiteConnect) throws KiteException, IOException {
        kiteConnect.cancelMFOrder("668604240868430");
        logger.info("cancel order successful");
    }

    /* retrieve all mutualfunds orders */
    public void getMFOrders(KiteConnect kiteConnect) throws KiteException, IOException {
        List<MFOrder> MFOrders = kiteConnect.getMFOrders();
        logger.info("mf orders: "+ MFOrders.size());
    }

    /* retrieve individual mutualfunds order */
    public void getMFOrder(KiteConnect kiteConnect) throws KiteException, IOException {
        logger.info("mf order: "+ kiteConnect.getMFOrder("106580291331583").tradingsymbol);
    }

    /* place mutualfunds sip */
    public void placeMFSIP(KiteConnect kiteConnect) throws KiteException, IOException {
        logger.info("mf place sip: "+ kiteConnect.placeMFSIP("INF174K01LS2", "monthly", 1, -1, 5000, 1000).sipId);
    }

    /* modify a mutual fund sip */
    public void modifyMFSIP(KiteConnect kiteConnect) throws KiteException, IOException {
        kiteConnect.modifyMFSIP("weekly", 1, 5, 1000, "active", "504341441825418");
    }

    /* cancel a mutualfunds sip */
    public void cancelMFSIP(KiteConnect kiteConnect) throws KiteException, IOException {
        kiteConnect.cancelMFSIP("504341441825418");
        logger.info("cancel sip successful");
    }

    /* retrieve all mutualfunds sip */
    public void getMFSIPS(KiteConnect kiteConnect) throws KiteException, IOException {
        List<MFSIP> sips = kiteConnect.getMFSIPs();
        logger.info("mf sips: "+ sips.size());
    }

    /* retrieve individual mutualfunds sip */
    public void getMFSIP(KiteConnect kiteConnect) throws KiteException, IOException {
        logger.info("mf sip: "+ kiteConnect.getMFSIP("291156521960679").instalments);
    }

    /** Demonstrates com.zerodhatech.ticker connection, subcribing for instruments, unsubscribing for instruments, set mode of tick data, com.zerodhatech.ticker disconnection*/
    public void tickerUsage(KiteConnect kiteConnect, final ArrayList<Long> tokens) throws IOException, WebSocketException, KiteException {
        /** To get live price use websocket connection.
         * It is recommended to use only one websocket connection at any point of time and make sure you stop connection, once user goes out of app.
         * custom url points to new endpoint which can be used till complete Kite Connect 3 migration is done. */
        final KiteTicker tickerProvider = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());

        tickerProvider.setOnConnectedListener(new OnConnect() {
            @Override
            public void onConnected() {
                /** Subscribe ticks for token.
                 * By default, all tokens are subscribed for modeQuote.
                 * */
                tickerProvider.subscribe(tokens);
                tickerProvider.setMode(tokens, KiteTicker.modeFull);
            }
        });

        tickerProvider.setOnDisconnectedListener(new OnDisconnect() {
            @Override
            public void onDisconnected() {
                // your code goes here
            }
        });

        /** Set listener to get order updates.*/
        tickerProvider.setOnOrderUpdateListener(new OnOrderUpdate() {
            @Override
            public void onOrderUpdate(Order order) {
                logger.info("order update "+order.orderId);
            }
        });

        /** Set error listener to listen to errors.*/
        tickerProvider.setOnErrorListener(new OnError() {
            @Override
            public void onError(Exception exception) {
                //handle here.
            }

            @Override
            public void onError(KiteException kiteException) {
                //handle here.
            }

            @Override
            public void onError(String error) {
                logger.info(error);
            }
        });

        tickerProvider.setOnTickerArrivalListener(new OnTicks() {
            @Override
            public void onTicks(ArrayList<Tick> ticks) {
                NumberFormat formatter = new DecimalFormat();
                logger.info("ticks size "+ticks.size());
                if(ticks.size() > 0) {
                    logger.info("last price "+ticks.get(0).getLastTradedPrice());
                    logger.info("open interest "+formatter.format(ticks.get(0).getOi()));
                    logger.info("day high OI "+formatter.format(ticks.get(0).getOpenInterestDayHigh()));
                    logger.info("day low OI "+formatter.format(ticks.get(0).getOpenInterestDayLow()));
                    logger.info("change "+formatter.format(ticks.get(0).getChange()));
                    logger.info("tick timestamp "+ticks.get(0).getTickTimestamp());
                    logger.info("tick timestamp date "+ticks.get(0).getTickTimestamp());
                    logger.info("last traded time "+ticks.get(0).getLastTradedTime());
                    logger.info(ticks.get(0).getMarketDepth().get("buy").size());
                }
            }
        });
        // Make sure this is called before calling connect.
        tickerProvider.setTryReconnection(true);
        //maximum retries and should be greater than 0
        tickerProvider.setMaximumRetries(10);
        //set maximum retry interval in seconds
        tickerProvider.setMaximumRetryInterval(30);

        /** connects to com.zerodhatech.com.zerodhatech.ticker server for getting live quotes*/
        tickerProvider.connect();

        /** You can check, if websocket connection is open or not using the following method.*/
        boolean isConnected = tickerProvider.isConnectionOpen();
        logger.info(isConnected);

        /** set mode is used to set mode in which you need tick for list of tokens.
         * Ticker allows three modes, modeFull, modeQuote, modeLTP.
         * For getting only last traded price, use modeLTP
         * For getting last traded price, last traded quantity, average price, volume traded today, total sell quantity and total buy quantity, open, high, low, close, change, use modeQuote
         * For getting all data with depth, use modeFull*/
        tickerProvider.setMode(tokens, KiteTicker.modeLTP);

        // Unsubscribe for a token.
        tickerProvider.unsubscribe(tokens);

        // After using com.zerodhatech.com.zerodhatech.ticker, close websocket connection.
        tickerProvider.disconnect();
    }
}
