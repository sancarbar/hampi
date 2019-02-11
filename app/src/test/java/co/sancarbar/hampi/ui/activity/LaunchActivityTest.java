package co.sancarbar.hampi.ui.activity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/**
 * @author Santiago Carrillo
 * 7/31/18.
 */

@RunWith( RobolectricTestRunner.class )
public class LaunchActivityTest
{

    @Test
    public void whenUserLoggedInThenHomeActivityIsLaunched()
    {
        LaunchActivity launchActivity = Robolectric.setupActivity( LaunchActivity.class );

    }
}
