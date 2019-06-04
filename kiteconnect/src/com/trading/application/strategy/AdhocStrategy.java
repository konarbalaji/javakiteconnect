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
import java.util.Map;

public class AdhocStrategy extends TradeExecutor {

    Logger logger = Logger.getLogger(AdhocStrategy.class.getName());
    Order buyOrder = null;
    Order sellOrder = null;
    double buyPrice = 0;
    double sellPrice = 10000;
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

        Instrument instrToTrade = null;

        try{

            boolean tradingIsOpen = true;
            while(tradingIsOpen){

                callMoneynessMap.clear();
                putMoneynessMap.clear();

                callMoneynessMap.putAll(findMoneyNess(futureName, Constants.CALL_OPTION));
                putMoneynessMap.putAll(findMoneyNess(futureName, Constants.PUT_OPTION));

                Instrument atmCE = getMoneynessInstrument(Constants.ATM, callMoneynessMap, bankNiftyOptionsMap, Constants.CE_INDICATOR);
                Instrument atmPE = getMoneynessInstrument(Constants.ATM, putMoneynessMap, bankNiftyOptionsMap, Constants.PE_INDICATOR);

                logger.info("ATM CE is >> " + atmCE.tradingsymbol);
                logger.info("ATM PE is >> " + atmPE.tradingsymbol);

                lastOhlc.reset();
                lastMinPriceList.clear();

                //Instrument ceOpt = callMoneynessMap.get(Constants.ATM);

                //findHighLow();
                //String currentTrend = findCurrentTrend();

                /*while (currentTrend.equalsIgnoreCase(Constants.FLAT)){
                    lastOhlc.reset();
                    lastMinPriceList.clear();

                    isPriceMovementBullish(atmCE, Constants.ANALYZE_INTERVAL);
                    currentTrend = findCurrentTrend();
                }*/

                boolean priceNotDecided = true;
                while(priceNotDecided){

                    /*Populates Price over last few seconds & also populates lastOHLC data */
                    String currentTrend = isPriceMovementBullish(atmCE, Constants.ANALYZE_INTERVAL);

                    if(currentTrend.equalsIgnoreCase(Constants.BEARISH)){
                        instrToTrade = atmPE;
                    }
                    else if(currentTrend.equalsIgnoreCase(Constants.BULLISH)){
                        instrToTrade = atmCE;
                    }

                    double buyPrice = 0;

                    Thread.sleep(500);

                    Map<String, Quote> optionSellerQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, instrToTrade);
                    double optionSellerPriceInMkt =  optionSellerQuote.get(Long.toString(instrToTrade.getInstrument_token())).depth.sell.get(0).getPrice();

                    printQuoteContent(instrToTrade);

                    buyPrice = optionSellerPriceInMkt-2;

                    String [] instrArr = {Long.toString(instrToTrade.getExchange_token())};
                    OHLCData ohlcData = exampleImpl.getOHLC(kiteConnect,instrArr);

                    /*Current Price is trading at higher range*/
                    if((ohlcData.high-4)< optionSellerPriceInMkt)
                        priceNotDecided=true;
                    else
                        priceNotDecided=false;

                }

                sellPrice = buyPrice + 1;
                logger.info(" ============= Buy " + instrToTrade.getTradingsymbol() + " @ " + buyPrice + " ============= ");
                Order buyOrder = placeBuyOrder(kiteConnect,Constants.QUANTITY,instrToTrade,Constants.TRANSACTION_TYPE_BUY, buyPrice);

                boolean notSold = true;
                while(notSold){

                    printQuoteContent(instrToTrade);

                    Map<String, Quote> optionBuyerQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, instrToTrade);
                    double optionBuyerPriceInMkt =  optionBuyerQuote.get(Long.toString(instrToTrade.getInstrument_token())).depth.buy.get(0).getPrice();

                    //optionBuyerPriceInMkt =  optionBuyerQuote.get(Long.toString(instrToTrade.getInstrument_token())).depth.buy.get(0).getPrice();
                    if(optionBuyerPriceInMkt>=sellPrice){
                        Order sellOrder = placeSellOrder(kiteConnect, buyOrder, Constants.QUANTITY, instrToTrade, Constants.TRANSACTION_TYPE_SELL, optionBuyerPriceInMkt);
                        logger.info(" ============= PROFIT ACHIEVED in MARKET DEPTH - SQUARE OFF ================= ");
                        logger.info(" ============= SELL " + instrToTrade.getTradingsymbol() + " @ " + optionBuyerPriceInMkt + " ============= ");
                        logger.info(" >>>>>>>>>>>>> PROFIT MARGIN = " + (optionBuyerPriceInMkt - buyPrice) + ">>>>>>>>>>>>>>>> ");
                        logger.info(" >>>>>>>>>>>>> No of Profitable Trades so far >> " + ++noOfProfitableOrders);
                        notSold = false;
                    }else{
                        notSold = true;
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