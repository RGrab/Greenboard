package run.greenboard.greenboard;

import android.util.Log;
import java.util.logging.*;

/**
 * Created by User on 11/26/2016.
 */

public class AndroidLoggingHandler extends Handler {

    public static void reset(Handler rootHandler) {}

    @Override
    public void close() {}

    @Override
    public void flush() {}

    @Override
    public void publish(LogRecord record) {
        if (!super.isLoggable(record)) {
            return;
        }

        String loggerName = record.getLoggerName();
        int maxLength = 30;
        String logTag = loggerName.length() > maxLength ? loggerName.substring(loggerName.length() - maxLength) : loggerName;
    }
}