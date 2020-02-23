package jn.mjz.aiot.jnuetc.kotlin.view.activity.settins

import android.os.Bundle
import android.view.Menu
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.view.custom.AbstractActivity


private const val TITLE_TAG = "settingsActivityTitle"

class SettingsActivity : AbstractActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private var isHomePage = true

    override fun preFinish(): Boolean {
        if (isHomePage) {
            XToast.success(getString(R.string.SettingSaveSuccessTip))
        }
        return true
    }

    override fun getOptionsMenuId(menu: Menu?): Int {
        return 0
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun getLayoutId(): Int {
        return R.layout.settings_activity
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, HeaderFragment())
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            isHomePage = supportFragmentManager.backStackEntryCount == 0
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_settings)
            }
        }
    }

    override fun initView() {
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment
        ).apply {
            arguments = args
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        title = pref.title
        return true
    }


    class HeaderFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = sharedPreferencesName
            setPreferencesFromResource(R.xml.header_preferences, rootKey)
        }
    }

    class BootFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = sharedPreferencesName
            setPreferencesFromResource(R.xml.boot_preferences, rootKey)
        }
    }

    class MainFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = sharedPreferencesName
            setPreferencesFromResource(R.xml.main_preferences, rootKey)
        }
    }

    class MingJuFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = sharedPreferencesName
            setPreferencesFromResource(R.xml.ming_ju_preferences, rootKey)
        }
    }

    class RankingFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = sharedPreferencesName
            setPreferencesFromResource(R.xml.ranking_preferences, rootKey)
        }
    }

    companion object {
        const val sharedPreferencesName = "settings"
    }

}
