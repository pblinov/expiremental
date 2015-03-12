import com.lmax.disruptor.EventHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.CountDownLatch;

/**
 * @author pblinov
 * @since 10/02/2015
 */
public class Handler implements EventHandler<Event> {
    private CountDownLatch latch;

    public Handler(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onEvent(Event event, long l, boolean b) throws Exception {
        SlowAction.doSomething(event);
        latch.countDown();
    }
}
