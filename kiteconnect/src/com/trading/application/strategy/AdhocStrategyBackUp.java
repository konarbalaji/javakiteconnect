package com.trading.application.strategy;

import com.trading.application.TradeExecutor;
import com.trading.application.utils.OHLCData;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.Quote;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdhocStrategyBackUp extends TradeExecutor {

    Logger logger = Logger.getLogger(AdhocStrategyBackUp.class.getName());
    List<Integer> lastMinutePrice = new ArrayList<Integer>();
    List<Integer> lastMinPriceList = null;
    Order buyOrder = null;
    Order sellOrder = null;
    double buyPrice = 0;
    double sellPrice = 1000;
    double mean = 0;


    public static void main(String[] args){

        try{

            AdhocStrategyBackUp adhoc = new AdhocStrategyBackUp();
            adhoc.setUpData();
            adhoc.adHocStrategy();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void adHocStrategy(){

        Instrument callOptionInstr = null;
        Instrument putOptionInstr = null;

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

                //Instrument ATMCEInstrument =  bankNiftyOptionsMap.get(callOptionInstr.getTradingsymbol());
                Map<String, Quote> AtmCEQuoteBuyLevel = exampleImpl.getQuoteForSingleInstrument(kiteConnect, callOptionInstr);
                printQuoteContent(callOptionInstr);
                double atmCEBuyPriceInMkt = AtmCEQuoteBuyLevel.get(Long.toString(callOptionInstr.getInstrument_token())).depth.buy.get(0).getPrice();
                logger.info("Buy Price of ATM_CE from market >> " + callOptionInstr.getTradingsymbol() + " >> " + atmCEBuyPriceInMkt);

                /*orderTracker.put(Constants.ATM_CE_BUY_ORD,buyCEOrder);*/

                //Instrument ATMPEInstrument =  bankNiftyOptionsMap.get(putOptionInstr.getTradingsymbol());
                Map<String, Quote> AtmPEQuoteBuyLevel = exampleImpl.getQuoteForSingleInstrument(kiteConnect, putOptionInstr);
                double atmPEBuyPriceInMkt = AtmPEQuoteBuyLevel.get(Long.toString(putOptionInstr.getInstrument_token())).depth.buy.get(0).getPrice();
                logger.info("Buy Price of ATM_PE from market >> " + putOptionInstr.getTradingsymbol() + " >> " + atmPEBuyPriceInMkt);
                /*placeBuyOrder(kiteConnect,20,putOptionInstr.tradingsymbol,Constants.TRANSACTION_TYPE_BUY, atmPEBuyPriceInMkt);
                Order buyPEOrder = placeBuyOrder(kiteConnect,Constants.QUANTITY,putOptionInstr.tradingsymbol,Constants.TRANSACTION_TYPE_BUY, atmPEBuyPriceInMkt);
                waitForOrderToBeFilled(buyPEOrder);
                orderTracker.put(Constants.ATM_PE_BUY_ORD,buyPEOrder);*/

                boolean tradingIsOpen = true;
                while(tradingIsOpen){

                    OHLCData lastOhlc = new OHLCData();
                    List<Double> lastMinPriceList = new ArrayList<>();
                    isPriceMovementBullish(bankNiftyFuture,Constants.ANALYZE_INTERVAL);
                    //findHighLow(lastOhlc, lastMinPriceList);
                    String currentTrend = findCurrentTrend();

                    while (currentTrend.equalsIgnoreCase(Constants.FLAT)){

                        currentTrend = findCurrentTrend();

                       /* List<Double> lastMinPriceList = new ArrayList<>();
                        isPriceMovementBullish(bankNiftyFuture, Constants.ANALYZE_INTERVAL);
                        findHighLow(lastOhlc, lastMinPriceList);
                        String currentTrend = findCurrentTrend(lastOhlc, lastMinPriceList);*/
                    }

                    callOptionInstr = getMoneynessInstrument(Constants.ITM1, callMoneynessMap, bankNiftyOptionsMap, Constants.CE_INDICATOR);
                    putOptionInstr = getMoneynessInstrument(Constants.ITM1, putMoneynessMap, bankNiftyOptionsMap, Constants.PE_INDICATOR);
                    Instrument instrToTrade = callOptionInstr;

                    if(currentTrend.equalsIgnoreCase(Constants.BEARISH))
                        instrToTrade = putOptionInstr;
                    else
                        instrToTrade = callOptionInstr;

                    /*int bearishTrendCount = 0;
                    while(isBearish){

                        lastOhlc.reset();
                        lastMinPriceList.clear();

                        isPriceMovementBullish(lastOhlc, callOptionInstr, lastMinPriceList, Constants.ANALYZE_INTERVAL);
                        findHighLow(lastOhlc, lastMinPriceList);
                        isBearish = findCurrentTrend(lastOhlc, lastMinPriceList);
                    }*/

                    //mean = findMean(lastOhlc);

                    /*Map<String, Quote> atmCESellerQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, callOptionInstr);
                    double atmCESellerPriceInMkt =  atmCESellerQuote.get(Long.toString(callOptionInstr.getInstrument_token())).depth.sell.get(0).getPrice();*/

                    boolean notBought = true;
                    double buyPrice = 0;
                    while(notBought){

                        Thread.sleep(1000);

                        Map<String, Quote> atmCESellerQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, instrToTrade);
                        double atmCESellerPriceInMkt =  atmCESellerQuote.get(Long.toString(instrToTrade.getInstrument_token())).depth.sell.get(0).getPrice();

                        printQuoteContent(instrToTrade);

                        //logger.info(callOptionInstr.getTradingsymbol() + " is trading @ " + atmCESellerPriceInMkt);


                        if(!(atmCESellerPriceInMkt >= lastOhlc.getHigh())){

                            buyPrice = atmCESellerPriceInMkt;
                            sellPrice = buyPrice + 1;
                            logger.info(" ============= Buy " + instrToTrade.getTradingsymbol() + " @ " + buyPrice + " ============= ");

                            //Order buyOrder = placeBuyOrder(kiteConnect,Constants.QUANTITY,callOptionInstr.tradingsymbol,Constants.TRANSACTION_TYPE_BUY, buyPrice);

                            notBought=false;
                        }
                    }

                    boolean notSold = true;
                    while(notSold){

                        printQuoteContent(instrToTrade);

                        Map<String, Quote> optionBuyerQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, instrToTrade);
                        double optionBuyerPriceInMkt =  optionBuyerQuote.get(Long.toString(instrToTrade.getInstrument_token())).depth.buy.get(0).getPrice();
                        //logger.info(callOptionInstr.getTradingsymbol() + " is trading @ " + optionBuyerPriceInMkt);

                        //boolean isSellTargetHit = optionBuyerPriceInMkt >= sellPrice;

                        /*while(isSellTargetHit){
                            catchRallyIfAny(callOptionInstr, sellPrice);
                            isSellTargetHit=false;
                        }*/

                        optionBuyerPriceInMkt =  optionBuyerQuote.get(Long.toString(instrToTrade.getInstrument_token())).depth.buy.get(0).getPrice();
                        if(optionBuyerPriceInMkt>=sellPrice){

                            //Order sellCEOrder = placeSellOrder(kiteConnect, buyOrder, Constants.QUANTITY, callOptionInstr.tradingsymbol, Constants.TRANSACTION_TYPE_SELL, sellPrice);

                            sellPrice = optionBuyerPriceInMkt;
                            logger.info(" ============= SELL " + instrToTrade.getTradingsymbol() + " @ " + sellPrice + " ============= ");
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