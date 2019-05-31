package com.trading.application.strategy;

import com.trading.application.TradeExecutor;
import com.trading.application.utils.OHLCData;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdhocStrategy extends TradeExecutor {

    Logger logger = Logger.getLogger(AdhocStrategy.class.getName());
    List<Integer> lastMinutePrice = new ArrayList<Integer>();
    List<Integer> lastMinPriceList = null;
    Order buyOrder = null;
    Order sellOrder = null;
    double buyPrice = 0;
    double sellPrice = 1000;
    double mean = 0;

    public static void main(String[] args){

        try{

            AdhocStrategy adhoc = new AdhocStrategy();
            adhoc.setUpData();
            adhoc.adHocStrategy();

        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void adHocStrategy(){

        Instrument atmCEInstrument = null;
        Instrument atmPEInstrument = null;

        try{

            while(noOfProfitableOrders<1000){

                /*boolean openPosition = false;
                Map<String, List<Position>> currPos = exampleImpl.getPositions(kiteConnect);

                int dayPositionSize = currPos.get("day").size();
                for(int i=0; i< dayPositionSize; i++){
                    int dayNetQty = currPos.get("day").get(i).netQuantity;
                    if(dayNetQty != 0){
                        logger.info("Net Position is >> " + dayNetQty);
                        openPosition = true;
                    }
                }

                while(openPosition){
                    logger.info("OPEN POSITION NEEDS TO BE CLOSSED");
                    System.exit(0);
                }*/

                atmCEInstrument = getMoneynessInstrument(Constants.ITM1, callMoneynessMap, bankNiftyOptionsMap, Constants.CE_INDICATOR);
                atmPEInstrument = getMoneynessInstrument(Constants.ITM1, putMoneynessMap, bankNiftyOptionsMap, Constants.PE_INDICATOR);

                //Instrument ATMCEInstrument =  bankNiftyOptionsMap.get(atmCEInstrument.getTradingsymbol());
                Map<String, Quote> AtmCEQuoteBuyLevel = exampleImpl.getQuoteForSingleInstrument(kiteConnect, atmCEInstrument);
                printQuoteContent(atmCEInstrument);
                double atmCEBuyPriceInMkt = AtmCEQuoteBuyLevel.get(Long.toString(atmCEInstrument.getInstrument_token())).depth.buy.get(0).getPrice();
                logger.info("Buy Price of ATM_CE from market >> " + atmCEInstrument.getTradingsymbol() + " >> " + atmCEBuyPriceInMkt);

                /*orderTracker.put(Constants.ATM_CE_BUY_ORD,buyCEOrder);*/

                //Instrument ATMPEInstrument =  bankNiftyOptionsMap.get(atmPEInstrument.getTradingsymbol());
                Map<String, Quote> AtmPEQuoteBuyLevel = exampleImpl.getQuoteForSingleInstrument(kiteConnect, atmPEInstrument);
                double atmPEBuyPriceInMkt = AtmPEQuoteBuyLevel.get(Long.toString(atmPEInstrument.getInstrument_token())).depth.buy.get(0).getPrice();
                logger.info("Buy Price of ATM_PE from market >> " + atmPEInstrument.getTradingsymbol() + " >> " + atmPEBuyPriceInMkt);
                /*placeBuyOrder(kiteConnect,20,atmPEInstrument.tradingsymbol,Constants.TRANSACTION_TYPE_BUY, atmPEBuyPriceInMkt);
                Order buyPEOrder = placeBuyOrder(kiteConnect,Constants.QUANTITY,atmPEInstrument.tradingsymbol,Constants.TRANSACTION_TYPE_BUY, atmPEBuyPriceInMkt);
                waitForOrderToBeFilled(buyPEOrder);
                orderTracker.put(Constants.ATM_PE_BUY_ORD,buyPEOrder);*/

                boolean tradingIsOpen = true;
                while(tradingIsOpen){

                    OHLCData lastOhlc = new OHLCData();
                    List<Double> lastMinPriceList = new ArrayList<>();
                    getDataForTimePeriod(lastOhlc, atmCEInstrument, lastMinPriceList, Constants.ANALYZE_INTERVAL);
                    findHighLow(lastOhlc, lastMinPriceList);
                    boolean isBearish = isBearish(lastOhlc, lastMinPriceList);

                    while(isBearish){
                        lastOhlc.reset();
                        lastMinPriceList.clear();

                        getDataForTimePeriod(lastOhlc, atmCEInstrument, lastMinPriceList, Constants.ANALYZE_INTERVAL);
                        findHighLow(lastOhlc, lastMinPriceList);
                        isBearish = isBearish(lastOhlc, lastMinPriceList);
                    }

                    //mean = findMean(lastOhlc);

                    /*Map<String, Quote> atmCESellerQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, atmCEInstrument);
                    double atmCESellerPriceInMkt =  atmCESellerQuote.get(Long.toString(atmCEInstrument.getInstrument_token())).depth.sell.get(0).getPrice();*/

                    boolean notBought = true;
                    double buyPrice = 0;
                    while(notBought){

                        Thread.sleep(1000);

                        Map<String, Quote> atmCESellerQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, atmCEInstrument);
                        double atmCESellerPriceInMkt =  atmCESellerQuote.get(Long.toString(atmCEInstrument.getInstrument_token())).depth.sell.get(0).getPrice();

                        printQuoteContent(atmCEInstrument);

                        //logger.info(atmCEInstrument.getTradingsymbol() + " is trading @ " + atmCESellerPriceInMkt);

                        if(!(atmCESellerPriceInMkt >= lastOhlc.getHigh())){

                            buyPrice = atmCESellerPriceInMkt;
                            sellPrice = buyPrice + 1;
                            logger.info(" ============= Buy " + atmCEInstrument.getTradingsymbol() + " @ " + buyPrice + " ============= ");

                            //Order buyOrder = placeBuyOrder(kiteConnect,Constants.QUANTITY,atmCEInstrument.tradingsymbol,Constants.TRANSACTION_TYPE_BUY, buyPrice);

                            notBought=false;
                        }
                    }

                    boolean notSold = true;
                    while(notSold){

                        printQuoteContent(atmCEInstrument);

                        Map<String, Quote> atmCEBuyerQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, atmCEInstrument);
                        double atmCEBuyerPriceInMkt =  atmCEBuyerQuote.get(Long.toString(atmCEInstrument.getInstrument_token())).depth.buy.get(0).getPrice();
                        //logger.info(atmCEInstrument.getTradingsymbol() + " is trading @ " + atmCEBuyerPriceInMkt);

                        //boolean isSellTargetHit = atmCEBuyerPriceInMkt >= sellPrice;

                        /*while(isSellTargetHit){
                            catchRallyIfAny(atmCEInstrument, sellPrice);
                            isSellTargetHit=false;
                        }*/

                        atmCEBuyerPriceInMkt =  atmCEBuyerQuote.get(Long.toString(atmCEInstrument.getInstrument_token())).depth.buy.get(0).getPrice();
                        if(atmCEBuyerPriceInMkt>=sellPrice){

                            //Order sellCEOrder = placeSellOrder(kiteConnect, buyOrder, Constants.QUANTITY, atmCEInstrument.tradingsymbol, Constants.TRANSACTION_TYPE_SELL, sellPrice);

                            sellPrice = atmCEBuyerPriceInMkt;
                            logger.info(" ============= SELL " + atmCEInstrument.getTradingsymbol() + " @ " + sellPrice + " ============= ");

                            logger.info(" =================  PROFIT ACHIEVED in MARKET DEPTH - SQUARE OFF ================= ");
                            logger.info(" >>>>>>>>>>>>>>>>>  PROFIT MARGIN = " + (sellPrice - buyPrice) + ">>>>>>>>>>>>>>>> ");
                            logger.info(" No of Profitable Trades so far >> " + ++noOfProfitableOrders);
                            notSold = false;
                        }else{
                            notSold = true;
                        }
                    }
                }
            }
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
    }





}