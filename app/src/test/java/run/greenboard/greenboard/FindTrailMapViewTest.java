package run.greenboard.greenboard;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import com.google.android.gms.maps.GoogleMap;

/**
 * Created by Ryan on 12/5/2016.
 */
public class FindTrailMapViewTest {
    private  FindTrailMapView findTrailMapView;
    @Mock
    GoogleMap mGooglemap;

    @Before
    public  void setClass(){
        findTrailMapView = mock(FindTrailMapView.class);
    }

    @Test
    public void calculateDistanceTest() {
        double clat = 0d;
        double clon = 0d;
        when(findTrailMapView.calculatedistance(clat,clon)).thenCallRealMethod();
        assertThat(findTrailMapView.calculatedistance(clat,clon),is(equalTo(0d)));
    }

    @Test
    public void calculateDistanceTest2() {
        double clat = 50d;
        double clon = 50d;
        when(findTrailMapView.calculatedistance(clat,clon)).thenCallRealMethod();
        assertThat(findTrailMapView.calculatedistance(clat,clon),is(not(equalTo(0d))));
    }
}