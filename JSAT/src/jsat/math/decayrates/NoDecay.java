package jsat.math.decayrates;

/**
 * A possible value for a decaying learning rate. NoDecay will perform no 
 * decaying of the initial value, the initial value will always be returned 
 * regardless of the input.
 * 
 * @author Edward Raff
 */
public class NoDecay implements DecayRate
{
    @Override
    public double rate(double time, double maxTime, double initial)
    {
        if(time < 0)
            throw new ArithmeticException("Negative time value given");
        return initial;
    }

    @Override
    public String toString()
    {
        return "NoDecay";
    }
}
