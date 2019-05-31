package com.trading.application.strategy;

import com.trading.application.TradeExecutor;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Position;
import com.zerodhatech.models.Quote;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StraddleStrategy extends  TradeExecutor {

    Logger logger = Logger.getLogger(TradeExecutor.class.getName());

    public void executeStraddle(){

        Instrument atmCEInstrument = null;
        Instrument atmPEInstrument = null;

        try{

            while(noOfProfitableOrders<100){

                boolean openPosition = false;
                Map<String, List<Position>> currPos = exampleImpl.getPositions(kiteConnect);
                int dayPositionSize = currPos.get("day").size();
                if(dayPositionSize > 0){
                    openPosition = true;
                }

                while(openPosition){
                    logger.info("OPEN POSITIONS NEEDS TO BE CLOSSED");
                    System.exit(0);
                }

                atmCEInstrument = getMoneynessInstrument(Constants.ATM, callMoneynessMap, bankNiftyOptionsMap, Constants.CE_INDICATOR);
                atmPEInstrument = getMoneynessInstrument(Constants.ATM, putMoneynessMap, bankNiftyOptionsMap, Constants.PE_INDICATOR);

                //Instrument ATMCEInstrument =  bankNiftyOptionsMap.get(atmCEInstrument.getTradingsymbol());
                Map<String, Quote> AtmCEQuoteBuyLevel = exampleImpl.getQuoteForSingleInstrument(kiteConnect, atmCEInstrument);
                double atmCEBuyPriceInMkt = AtmCEQuoteBuyLevel.get(Long.toString(atmCEInstrument.getInstrument_token())).depth.buy.get(0).getPrice();
                logger.info("Buy Price of ATM_CE from market >> " + atmCEInstrument.getTradingsymbol() + " >> " + atmCEBuyPriceInMkt);
               /* Order buyCEOrder = placeBuyOrder(kiteConnect,Constants.QUANTITY,atmCEInstrument.tradingsymbol,Constants.TRANSACTION_TYPE_BUY, atmCEBuyPriceInMkt);
                waitForOrderToBeFilled(buyCEOrder);
                orderTracker.put(Constants.ATM_CE_BUY_ORD,buyCEOrder);*/

                //Instrument ATMPEInstrument =  bankNiftyOptionsMap.get(atmPEInstrument.getTradingsymbol());
                Map<String, Quote> AtmPEQuoteBuyLevel = exampleImpl.getQuoteForSingleInstrument(kiteConnect, atmPEInstrument);
                double atmPEBuyPriceInMkt = AtmPEQuoteBuyLevel.get(Long.toString(atmPEInstrument.getInstrument_token())).depth.buy.get(0).getPrice();
                logger.info("Buy Price of ATM_PE from market >> " + atmPEInstrument.getTradingsymbol() + " >> " + atmPEBuyPriceInMkt);
                /*placeBuyOrder(kiteConnect,20,atmPEInstrument.tradingsymbol,Constants.TRANSACTION_TYPE_BUY, atmPEBuyPriceInMkt);
                Order buyPEOrder = placeBuyOrder(kiteConnect,Constants.QUANTITY,atmPEInstrument.tradingsymbol,Constants.TRANSACTION_TYPE_BUY, atmPEBuyPriceInMkt);
                waitForOrderToBeFilled(buyPEOrder);
                orderTracker.put(Constants.ATM_PE_BUY_ORD,buyPEOrder);*/

                boolean noProfitMargin = true;
                while(noProfitMargin){

                    /*double AtmCESellPriceLTP = exampleImpl.getLTPOfSingleInstrument(kiteConnect, atmCEInstrument);
                    double AtmPESellPriceLTP = exampleImpl.getLTPOfSingleInstrument(kiteConnect, atmPEInstrument);*/

                    Map<String, Quote> atmCEQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, atmCEInstrument);
                    double atmCESellPriceInMkt =  atmCEQuote.get(Long.toString(atmCEInstrument.getInstrument_token())).depth.buy.get(0).getPrice();

                    Map<String, Quote> atmPEQuote = exampleImpl.getQuoteForSingleInstrument(kiteConnect, atmPEInstrument);
                    double atmPESellPriceInMkt =  atmPEQuote.get(Long.toString(atmPEInstrument.getInstrument_token())).depth.buy.get(0).getPrice();

                    double CePemargin = (atmCESellPriceInMkt-atmCEBuyPriceInMkt) + (atmPESellPriceInMkt-atmPEBuyPriceInMkt);

                    String actMarginFromMarketDepthStr = "(" + atmCESellPriceInMkt + "-" + atmCEBuyPriceInMkt + ")" + "+(" + atmPESellPriceInMkt + "-" + atmPEBuyPriceInMkt + ")";
                    double actMarginFromMarketDepth = ((atmCESellPriceInMkt-atmCEBuyPriceInMkt) + (atmPESellPriceInMkt-atmPEBuyPriceInMkt));
                    actMarginFromMarketDepthStr = actMarginFromMarketDepthStr + padSpace(actMarginFromMarketDepthStr);

                    logger.info(actMarginFromMarketDepthStr +  " ==========> " + CePemargin);

                    if(CePemargin > 3) {

                        logger.info(actMarginFromMarketDepthStr +  " ==========> " + actMarginFromMarketDepth);

                        noProfitMargin = false;
                        logger.info(" =================  PROFIT ACHIEVED in MARKET DEPTH - SQUARE OFF ================= ");

                        /*Order parentBuyOrderCE = orderTracker.get(Constants.ATM_CE_BUY_ORD);
                        Order sellCEOrder = placeSellOrder(kiteConnect, parentBuyOrderCE, Constants.QUANTITY, atmCEInstrument.tradingsymbol, Constants.TRANSACTION_TYPE_SELL, atmCESellPriceInMkt);

                        Order parentBuyOrderPE = orderTracker.get(Constants.ATM_PE_BUY_ORD);
                        Order sellPEOrder = placeSellOrder(kiteConnect, parentBuyOrderPE, Constants.QUANTITY, atmPEInstrument.tradingsymbol, Constants.TRANSACTION_TYPE_SELL, atmPESellPriceInMkt);

                        waitForSquareOff(sellCEOrder);
                        waitForSquareOff(sellPEOrder);*/

                        logger.info("SELL Price of ATM_CE from market depth >> " + atmCESellPriceInMkt);
                        logger.info("SELL Price of ATM_PE from market depth >> " + atmPESellPriceInMkt);

                        logger.info("No of ProfitableOrders placed so far >> " + ++noOfProfitableOrders);

                    }
                }
            }

        }catch (KiteException e) {
            logger.error(e.message+" "+e.code+" "+e.getClass().getName());
        } catch (JSONException e) {
            logger.error(e.getMessage()+" "+" "+e.getClass().getName());
            e.printStackTrace();
        }catch (IOException e) {
            logger.error(e.getMessage()+" "+" "+e.getClass().getName());
            e.printStackTrace();
        } catch(Exception e){
            logger.error(e.getMessage()+" "+" "+e.getClass().getName());
            e.printStackTrace();
        }
    }

}
