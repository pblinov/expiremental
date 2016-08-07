import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

/**
 * @author pblinov
 * @since 13/03/2015
 */
public class Test1 {
    public void testTwoMemberMapSizes() {
        // start the first member
        HazelcastInstance h1 = Hazelcast.newHazelcastInstance();
        // get the map and put 1000 entries
        Map map1 = h1.getMap( "testmap" );
        for ( int i = 0; i < 1000; i++ ) {
            map1.put( i, "value" + i );
        }
        // check the map size
        //assertEquals( 1000, map1.size() );
        // start the second member
        HazelcastInstance h2 = Hazelcast.newHazelcastInstance();
        // get the same map from the second member
        Map map2 = h2.getMap( "testmap" );
        // check the size of map2
        //assertEquals( 1000, map2.size() );
        // check the size of map1 again
        //assertEquals( 1000, map1.size() );
    }
}
