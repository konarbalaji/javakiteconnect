package com.trading.application.utils;
import org.apache.log4j.Logger;

public class OHLCData {

    public double high;
    public double low;
    public double close;
    public double open;

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public void reset(){
        this.setHigh(0);
        this.setLow(0);
        this.setOpen(0);
        this.setClose(0);
    }
}
