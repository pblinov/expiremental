package aeron;

import uk.co.real_logic.aeron.CncFileDescriptor;
import uk.co.real_logic.aeron.CommonContext;
import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.agrona.IoUtil;
import uk.co.real_logic.agrona.concurrent.AtomicBuffer;
import uk.co.real_logic.agrona.concurrent.CountersReader;
import uk.co.real_logic.agrona.concurrent.SigInt;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author pblinov
 * @since 29/09/2017
 */
public class TestAeronStat {
    public static void main(final String[] args) throws Exception
    {
        final File cncFile = new File("C:\\cygwin64\\tmp\\aeron-pblinov\\cnc.dat");//CommonContext.newDefaultCncFile();
        System.out.println("Command `n Control file " + cncFile);

        final MappedByteBuffer cncByteBuffer = IoUtil.mapExistingFile(cncFile, "cnc");
        final DirectBuffer metaDataBuffer = CncFileDescriptor.createMetaDataBuffer(cncByteBuffer);
        final int cncVersion = metaDataBuffer.getInt(CncFileDescriptor.cncVersionOffset(0));
        final long clientLiveness = metaDataBuffer.getLong(CncFileDescriptor.clientLivenessTimeoutOffset(0));

//        if (CncFileDescriptor.CNC_VERSION != cncVersion)
//        {
//            throw new IllegalStateException("CNC version not supported: version=" + cncVersion);
//        }

        final AtomicBuffer labelsBuffer = CncFileDescriptor.createCountersValuesBuffer(cncByteBuffer, metaDataBuffer);
        final AtomicBuffer valuesBuffer = CncFileDescriptor.createCountersValuesBuffer(cncByteBuffer, metaDataBuffer);
        final CountersReader countersReader = new CountersReader(labelsBuffer, valuesBuffer);

        // Setup the SIGINT handler for graceful shutdown
        final AtomicBoolean running = new AtomicBoolean(true);
        SigInt.register(() -> running.set(false));

        while (running.get())
        {
            System.out.print("\033[H\033[2J");
            System.out.format("%1$tH:%1$tM:%1$tS - Aeron Stat", new Date());
            System.out.format(" (CnC v%d), client liveness %,d ns\n", cncVersion, clientLiveness);
            System.out.println("=========================");

            countersReader.forEach(
                    (id, label) ->
                    {
                        final long value = countersReader.getCounterValue(id);
                        System.out.format("%3d: %,20d - %s\n", id, value, label);
                    });

            Thread.sleep(1000);
        }
    }
}
