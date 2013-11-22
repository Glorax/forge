package forge;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import forge.util.ThreadUtil;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class FThreads {
    
    private FThreads() { } // no instances supposed
    /** Checks if calling method uses event dispatch thread.
     * Exception thrown if method is on "wrong" thread.
     * A boolean is passed to indicate if the method must be EDT or not.
     * 
     * @param methodName &emsp; String, part of the custom exception message.
     * @param mustBeEDT &emsp; boolean: true = exception if not EDT, false = exception if EDT
     */
    public static void assertExecutedByEdt(final boolean mustBeEDT) {
        if (isGuiThread() != mustBeEDT ) { 
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            final String methodName =  trace[2].getClassName() + "." + trace[2].getMethodName();
            String modalOperator = mustBeEDT ? " must be" : " may not be";
            throw new IllegalStateException( methodName + modalOperator + " accessed from the event dispatch thread.");
        }
    }

    /**
     * TODO: Write javadoc for this method.
     * @param runnable
     */
    public static void invokeInEdtLater(Runnable runnable) {
        SwingUtilities.invokeLater(runnable);
    }

    /**
     * TODO: Write javadoc for this method.
     */
    public static void invokeInEdtNowOrLater(Runnable proc) {
        if( isGuiThread() )
            proc.run();
        else
            invokeInEdtLater(proc);
    }

    /**
     * Invoke the given Runnable in an Event Dispatch Thread and wait for it to
     * finish; but <B>try to use SwingUtilities.invokeLater instead whenever
     * feasible.</B>
     * 
     * Exceptions generated by SwingUtilities.invokeAndWait (if used), are
     * rethrown as RuntimeExceptions.
     * 
     * @param proc
     *            the Runnable to run
     * @see javax.swing.SwingUtilities#invokeLater(Runnable)
     */
    public static void invokeInEdtAndWait(final Runnable proc) {
        if (SwingUtilities.isEventDispatchThread()) {
            // Just run in the current thread.
            proc.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(proc);
            } catch (final InterruptedException exn) {
                throw new RuntimeException(exn);
            } catch (final InvocationTargetException exn) {
                throw new RuntimeException(exn);
            }
        }
    }
    
    

    
    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public static boolean isGuiThread() {
        return SwingUtilities.isEventDispatchThread();
    }


    public static void delayInEDT(int milliseconds, final Runnable inputUpdater) {
        Runnable runInEdt = new Runnable() {
            @Override public void run() {
                FThreads.invokeInEdtNowOrLater(inputUpdater);
            }
        };
        ThreadUtil.delay(milliseconds, runInEdt);
    }
    
    public static String debugGetCurrThreadId() {
        return isGuiThread() ? "EDT" : Thread.currentThread().getName();
    }

    public static String prependThreadId(String message) {
        return debugGetCurrThreadId() + " > " + message;
    }
    
    public static void dumpStackTrace(PrintStream stream) {
      StackTraceElement[] trace = Thread.currentThread().getStackTrace();
      stream.printf("%s > %s called from %s%n", debugGetCurrThreadId(), trace[2].getClassName()+"."+trace[2].getMethodName(), trace[3].toString());
      int i = 0;
      for(StackTraceElement se : trace) {
          if(i<2) i++;
          else stream.println(se.toString());
      }
    }

    public static String debugGetStackTraceItem(int depth, boolean shorter) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        String lastItem = trace[depth].toString();
        if( shorter ) {
            int lastPeriod = lastItem.lastIndexOf('.');
            lastPeriod = lastItem.lastIndexOf('.', lastPeriod-1);
            lastPeriod = lastItem.lastIndexOf('.', lastPeriod-1);
            lastItem = lastItem.substring(lastPeriod+1);
            return String.format("%s > from %s", debugGetCurrThreadId(), lastItem);
        }
        return String.format("%s > %s called from %s", debugGetCurrThreadId(), trace[2].getClassName()+"."+trace[2].getMethodName(), lastItem);
    }

    public static String debugGetStackTraceItem(int depth) {
        return debugGetStackTraceItem(depth, false);
    }
}
