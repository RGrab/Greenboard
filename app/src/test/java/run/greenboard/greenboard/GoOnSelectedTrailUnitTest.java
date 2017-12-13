package run.greenboard.greenboard;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import android.content.SharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.EditText;

import com.facebook.AccessToken;

import junit.framework.Assert;

import java.lang.reflect.*;
import java.util.regex.Matcher;


/**
 * Created by Ryan on 12/4/2016.
 */


public class GoOnSelectedTrailUnitTest {
    private GoOnSelectedTrail goOnSelectedTrail;


    @Before
    public void setClass() {
        goOnSelectedTrail = mock(GoOnSelectedTrail.class);
    }

    @Test
    public void calculateDistanceTest() {
        double clat = 0d;
        double clon = 0d;
        when(goOnSelectedTrail.calculateDistance(clat,clon)).thenCallRealMethod();
        assertThat(goOnSelectedTrail.calculateDistance(clat,clon),is(equalTo(0d)));
    }

    @Test
    public void calculateDistanceTest2() {
    double clat = 50d;
    double clon = 50d;
        when(goOnSelectedTrail.calculateDistance(clat,clon)).thenCallRealMethod();
        assertThat(goOnSelectedTrail.calculateDistance(clat,clon),is(not(equalTo(0d))));
    }
}