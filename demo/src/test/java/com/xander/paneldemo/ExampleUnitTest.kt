package com.xander.paneldemo

import com.xander.panel.XanderPanel.Builder.setTitle
import com.xander.panel.XanderPanel.Builder.setIcon
import com.xander.panel.XanderPanel.Builder.setMessage
import com.xander.panel.XanderPanel.Builder.setGravity
import com.xander.panel.XanderPanel.Builder.setController
import com.xander.panel.XanderPanel.Builder.setCanceledOnTouchOutside
import com.xander.panel.XanderPanel.Builder.setSheet
import com.xander.panel.XanderPanel.Builder.list
import com.xander.panel.XanderPanel.Builder.setMenu
import com.xander.panel.XanderPanel.Builder.grid
import com.xander.panel.XanderPanel.Builder.shareText
import com.xander.panel.XanderPanel.Builder.shareImages
import com.xander.panel.XanderPanel.Builder.setView
import com.xander.panel.XanderPanel.Builder.create
import com.xander.panel.XanderPanel.show
import androidx.appcompat.app.AppCompatActivity
import com.xander.paneldemo.R
import android.view.LayoutInflater
import android.os.Bundle
import android.view.Gravity
import com.xander.panel.PanelInterface.PanelControllerListener
import com.xander.panel.XanderPanel
import com.xander.panel.PanelInterface.SheetListener
import com.xander.panel.PanelInterface.PanelMenuListener
import android.widget.Toast
import org.junit.Assert
import org.junit.Test
import java.lang.Exception
import kotlin.Throws

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class ExampleUnitTest {
    @Test
    @Throws(Exception::class)
    fun addition_isCorrect() {
        Assert.assertEquals(4, (2 + 2).toLong())
    }
}