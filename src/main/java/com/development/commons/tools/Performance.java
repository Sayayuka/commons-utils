package com.development.commons.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to log performance stats in the log file. This class is thread safe as long as you do not share the
 * PerfStat object that is returned.
 *
 * All performance logging logs at DEBUG level, and can be filtered out by this Performance class
 * (Performance).
 *
 * @author ddiodati
 *
 */
public class Performance {

  private static final double MILLIS_PER_SECOND = 1000D;

  private Performance() {
    super();
  }

  /**
   * Starts a timer for later logging.
   *
   * @param message
   *          The final message to print when finishTimer is called.
   *
   * @return A new Peformance stat object that must be passed to later calls. This object should only be saved until the
   *         finishTimer method is called.
   */
  public static final PerfStat startTimer(final String message) {
    final PerfStat p = new PerfStat(true);
    p.msg = message;
    return p;
  }

  /**
   * Adds a stop point after a timer has been started. Allows to time sub sections of an area that is being timed. Each
   * stop point will be the elapsed time from the previous stop point or the startTimer call. For example: PerfStat p =
   * Performance.startTimer(); // do some task
   *
   * // finish some import sub task Performance.addStopPoint(p);
   *
   * // finish some other import sub task Performance.addStopPoint(p);
   *
   * // finish the entire task Performance.finishTimer(p, log);
   *
   * @param ps
   *          The PerfStat The performance object created by the previous call to startTimer().
   * @param message
   *          The message to print for this stop point when finishTimer is called.
   */
  public static final void addStopPoint(final PerfStat ps, final String message) {
    final PerfStat p = new PerfStat(false);
    p.endTime = System.currentTimeMillis();

    if (ps.times == null) {
      ps.times = new ArrayList<PerfStat>();
    }

    p.msg = message;
    ps.times.add(p);

  }

  /**
   * Indicates if performance logging is enabled.
   *
   * @return whether or not debug is enabled
   */
  public static final boolean isEnabled() {
    return false; //log.isDebugEnabled();
  }

  /**
   * Prints final stats on this timer. This includes the total time and any stop points that were set along the way.
   *
   * @param ps
   *          The performance object that was returned by the startTimer method.
   * @param clazz
   *          The class that is using the timer. Used for logging.
   *
   *          For example, finishTimer(ps, getClass());
   */
  public static final void finishTimer(final PerfStat ps, final Class<?> clazz) {

    if (isEnabled()) {
      ps.endTime = System.currentTimeMillis();

      final String className = getClassName(clazz);

      final StringBuilder buf = new StringBuilder();

      String prefix = "[PERFORMANCE] [";
      prefix += Thread.currentThread().getName() + "] ";

      if (ps.times != null) {
        buf.append(prefix).append(className).append("\n");
        for (int i = 0; i < ps.times.size(); i++) {
          final PerfStat p = ps.times.get(i);

          // obtains the difference in timer from one stop time to the next
          // if it is the first one uses the initial/overall start time.
          long startTime = ps.startTime;
          if (i > 0) {
            startTime = ps.times.get(i - 1).endTime;
          }

          buf.append(prefix).append("StopPoint Elapsed Time(secs):")
              .append((p.endTime - startTime) / MILLIS_PER_SECOND);
          buf.append(": ").append(p.msg).append("\n");
        }
      }

      buf.append(prefix).append(className).append(": Total Elapsed time(secs):")
          .append((ps.endTime - ps.startTime) / MILLIS_PER_SECOND).append(": ").append(ps.msg).append("\n");

//      log.debug(buf.toString());
    }
  }

  private static String getClassName(final Class<?> clazz) {
    if (clazz != null) {
      String s = clazz.getName();
      s = s.substring(s.lastIndexOf(".") + 1);
      return "[" + s + "]";
    }
    return "";
  }

  public static final class PerfStat {
    long startTime;
    long endTime;

    List<PerfStat> times;
    String msg;

    PerfStat(final boolean start) {
      if (start) {
        startTime = System.currentTimeMillis();
      }
    }

  }

}
