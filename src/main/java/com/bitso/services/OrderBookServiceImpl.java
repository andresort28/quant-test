package com.bitso.services;

import com.bitso.model.Market;
import com.bitso.model.Order;
import com.bitso.model.OrderSide;
import com.bitso.repository.OrderBook;
import com.bitso.repository.OrderBookRepository;
import com.bitso.repository.OrderBookRepositoryImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Implementation of {@link OrderBookService}
 *
 * @author Andres Ortiz
 */
@Slf4j
public class OrderBookServiceImpl implements OrderBookService {

    private static OrderBookServiceImpl INSTANCE;
    private final OrderBookRepository orderBookRepository = OrderBookRepositoryImpl.getInstance();

    /**
     * Get Singleton instance
     *
     * @return
     */
    public static OrderBookService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OrderBookServiceImpl();
        }
        return INSTANCE;
    }

    @Override
    public Queue<Order> getBuyOrders(Market market, double price) {
        return orderBookRepository.getBidOrders(market, price);
    }

    @Override
    public Queue<Order> getSellOrders(Market market, double price) {
        return orderBookRepository.getAskOrders(market, price);
    }

    @Override
    public boolean orderBookExist(Market market) {
        OrderBook orderBook = orderBookRepository.getOrderBook(market);
        return orderBook != null;
    }

    @Override
    public void printOrderBook(Market market) {
        log.debug("--OrderBook Map");
        OrderBook orderBook = orderBookRepository.getOrderBook(market);
        log.debug("----Market: {}", market);
        log.debug("------Ask Orders:");
        printSide(orderBook.getAskOrders());
        log.debug("------Bid Orders:");
        printSide(orderBook.getBidOrders());
    }

    @Override
    public double getEquilibriumMidMarketPrice(Market market, double halfLife) {
        OrderBook orderBook = orderBookRepository.getOrderBook(market);

        double[] pointsBidDecay = getPointsLastDecay(market, orderBook.getBidOrders(), OrderSide.BUY, halfLife);
        log.debug("Bid Points: (x1={}, y1={}) & (x2={}, y2={}) to create y=a*b^(-x)", pointsBidDecay[0], pointsBidDecay[1], pointsBidDecay[2], pointsBidDecay[3]);
        double[] expDecayValues = getExponentialDecayValues(pointsBidDecay);
        log.debug("Exponential Decay formula: y={}*{}^(-x)", expDecayValues[0], expDecayValues[1]);
        log.debug("----");
        double[] pointsAskDecay = getPointsLastDecay(market, orderBook.getAskOrders(), OrderSide.SELL, halfLife);
        log.debug("Ask Points: (x1={}, y1={}) & (x2={}, y2={}) to create y=a*b^(x)", pointsAskDecay[0], pointsAskDecay[1], pointsAskDecay[2], pointsAskDecay[3]);
        double[] expGrowthValues = getExponentialGrowthValues(pointsAskDecay);
        log.debug("Exponential Growth formula: y={}*{}^(x)", expGrowthValues[0], expGrowthValues[1]);

        /*
            Exponential Decay Formula (Big Orders)      -> y=a*b^(-x)   ~ y=a1*b1^(-x)
            Exponential Growth Formula (Ask Orders)     -> y=a*b^(x)    ~ y=a2*b2^(x)

            Equilibrium Mid-Market Price is found by equating above formulas to find the value of "x" given by the following summary formula:
            x = log base b2*b1 of a1/a2
            x = Math.log(a1/a2) / Math.log(b2*b1);
         */
        return Math.log(expDecayValues[0] / expGrowthValues[0]) / Math.log(expGrowthValues[1] * expDecayValues[1]);
    }

    /**
     * Bonus Track
     * <p>
     * Get the last two coordinates of the Exponential Decay
     *
     * @param market
     * @param orders
     * @param side
     * @param hl
     * @return
     */
    private double[] getPointsLastDecay(Market market, ConcurrentMap<Double, PriorityBlockingQueue<Order>> orders, OrderSide side, double hl) {
        /*
            Exponential Decay Formula y=A*F^(Δ/H)
            A = Previous value
            F = Decay in %
            Δ = Delta in x-axis
            H = Half life
         */
        final double decay = 0.5;       //Decay of 50% of the price
        final double delta = 1;         //Minimum price unit (for the sample case in the Script is 1)

        TreeSet<Double> prices;
        String message;
        if (side == OrderSide.BUY) {
            prices = new TreeSet<>(orders.keySet());
            message = "Lowest Bid Price: {}";
        } else {
            prices = (TreeSet<Double>) new TreeSet<>(orders.keySet()).descendingSet();
            message = "Highest Ask Price: {}";
        }
        log.debug(message, prices.first());

        double prevPrice = 0;
        double currPrice = prices.first();

        double prevValue = 0;
        double currValue = totalAmount(market, side, currPrice);
        log.debug("----f({}) = {}", currPrice, currValue);

        prices.remove(currPrice);
        for (Double price : prices) {
            currPrice = price;
            prevValue = currValue;
            currValue = (prevValue * Math.pow(decay, (delta / hl))) + totalAmount(market, side, price);
            log.debug("----f({}) = {}*{}^({}/{}) + TotalAmount({})", price, prevValue, decay, delta, hl, price);
            log.debug("----f({}) = {}", currPrice, currValue);
        }
        prevPrice = currPrice;
        if (side == OrderSide.BUY) {
            currPrice = currPrice + delta;
        } else {
            currPrice = currPrice - delta;
        }
        prevValue = currValue;
        currValue = (prevValue * Math.pow(decay, (delta / hl)));
        log.debug("----f({}) = {}*{}^({}/{}) + TotalAmount({})", currPrice, prevValue, decay, delta, hl, currPrice);
        log.debug("----f({}) = {}", currPrice, currValue);

        /*
            Coordinates to obtain the exponential equations
            (x1,y1) = (prevPrice,prevValue)
            (x2,y2) = (currPrice,currValue)
         */
        return new double[]{prevPrice, prevValue, currPrice, currValue};
    }

    /**
     * Bonus Track
     * <p>
     * Get the total of amount on one specific price given the {@link OrderSide} and {@link Market}
     *
     * @param market
     * @param side
     * @param price
     * @return
     */
    private double totalAmount(Market market, OrderSide side, double price) {
        Queue<Order> orders;
        if (side == OrderSide.BUY) {
            orders = getBuyOrders(market, price);
        } else {
            orders = getSellOrders(market, price);
        }
        double sum = orders.stream()
                .mapToDouble(Order::getAmount)
                .sum();
        log.debug("----TotalAmount({}) = {}", price, sum);
        return sum;
    }

    /**
     * Bonus Track
     * <p>
     * Get the main values of the general Exponential Decay formula
     *
     * @param points
     * @return
     */
    private double[] getExponentialDecayValues(double[] points) {
        /*
            Exponential Decay Formula (Big Orders) -> y=a*b^(-x)
            Given two points (x1,y1) & (x2,y2), the variable "a" and "b" can be resolved as follows:

            b = y1/y2
            a = y2*b^(x2)
         */
        final double b = points[1] / points[3];
        final double a = points[3] * Math.pow(b, points[2]);
        return new double[]{a, b};
    }

    /**
     * Bonus Track
     * <p>
     * Get the main values of the general Exponential Growth formula
     *
     * @param points
     * @return
     */
    private double[] getExponentialGrowthValues(double[] points) {
        /*
            Exponential Growth Formula (Big Orders) -> y=a*b^(x)
            Given two points (x1,y1) & (x2,y2), the variable "a" and "b" can be resolved as follows:

            b = (y2/y1)^(1/(x2-x1))
            a = y2 / b^(x2)
         */
        final double b = Math.pow(points[3] / points[1], 1 / (points[2] - points[0]));
        final double a = points[3] / Math.pow(b, points[2]);
        return new double[]{a, b};
    }

    /**
     * Print the sorted Orders of a side (Ask or Bid)
     * At time to sort the keys to print them in desc order, the time complexity could go from O(1) to O(nlogn) in the worst case
     *
     * @param sideOrders
     */
    private void printSide(ConcurrentMap<Double, PriorityBlockingQueue<Order>> sideOrders) {
        TreeSet<Double> keys = (TreeSet<Double>) new TreeSet<>(sideOrders.keySet()).descendingSet();
        keys.forEach(price -> {
            log.debug("--------Price $ {}:", price);
            sideOrders.get(price).forEach(order -> log.debug("----------{}", order));
        });
    }

    private OrderBookServiceImpl() {
    }
}
