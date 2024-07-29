package io.github.dnv825.elapsedtimecounter

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import io.github.dnv825.elapsedtimecounter.databinding.ActivityMainBinding
import java.time.LocalDateTime
import java.util.Timer
import java.util.concurrent.Future
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask

/**
 * メインアクティビティ。
 *
 * elapse time counterのメインアクティビティ。
 */
class MainActivity : AppCompatActivity() {

    //------------------------
    // 自動生成されたメンバ変数。
    //------------------------
    /** （多分自動生成されたもので、よくわからない…。） */
    private lateinit var appBarConfiguration: AppBarConfiguration

    /** （Bindingがよくわからない…。） */
    private lateinit var binding: ActivityMainBinding

    //------------------
    // Log.d()用の定数。
    //------------------
    /** Log.d()の第1引数へ指定するクラス名。 */
    private val TAG  = MainActivity::class.java.getSimpleName()

    //----------------------------
    // タイマー用のメンバ変数と定数。
    //----------------------------
    /** タイマーのインターバル間隔。1秒ごとにタイマーを進めたいので、1000[ms]とする。 */
    private val INTERVAL = 1000L

    /** タイマーを割り当てるメンバ変数。timerは動かしっぱなしにし、そのうえで1秒ごとに画面表示を更新するか判定する。 */
    private var timer = Timer()

    /** （何に使うのかよくわかっていない。） */
    private val handler : Handler = Handler(Looper.getMainLooper())

    /** 履歴を保存するファイル名。 */
    val FILE_NAME = "elapsed_time.log"

    //------------------------
    // アプリの状態を表す定数。
    // 列挙体にすべきところだが、kotlinの列挙体について調べるのが面倒なので、とりあえず定数で表現する。
    //------------------------
    /** 「計測開始前」を意味する定数。PLAYボタンを表示する状態のこと。 */
    val PLAY_STATE = 0

    /** 「計測中」を意味する定数。STOPボタンを表示する状態のこと。 */
    val STOP_STATE = 1

    /** 「計測終了後」を意味する定数。REFRESHボタンを表示する状態のこと。 */
    val REFRESH_STATE = 2

    /** アプリの状態を格納するメンバ変数。初期値は「計測開始前」とする。 */
    private var app_state = PLAY_STATE

    /**
     * アプリの状態を返却する。
     *
     * アプリが「計測開始前」・「計測中」・「計測終了後」のいずれの状態であるかを返却する。
     *
     * @return PLAY_STATE/STOP_STATE/REFRESH_STATEのいずれか。
     */
    fun getAppState(): Int {
        return app_state
    }

    /**
     * アプリの状態をセットする。
     *
     * アプリが「計測開始前」・「計測中」・「計測終了後」のいずれの状態であるかをセットする。
     *
     * @param state PLAY_STATE/STOP_STATE/REFRESH_STATEのいずれか。
     */
    fun setAppState(state: Int) {
        app_state = state
    }

    /**
     * アクティビティを生成する。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //----------------------
        // （自動生成された処理。）
        //----------------------
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        //-------------------------------------------------------------
        // アプリの初回起動時は履歴ファイルが存在しないので、ファイルを作成する。
        // 履歴ファイルに空文字を追記することでこの処理に代える。
        //-------------------------------------------------------------
        appendHistory("")

        //----------------------------------------------------------------
        // フローティングアクションボタンにGMail呼び出し処理をセットする。
        // 厳密にはGMail呼び出しではなく、メールアプリの汎用呼び出しといったところ。
        //----------------------------------------------------------------
        binding.fab.setOnClickListener { view ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_TEXT, readHistory().replace("\t", "|"))
            }

            // GMailの呼び出しに失敗した場合、スナックバーにその旨を表示する。
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Snackbar.make(view, getString(R.string.failed_to_invoke_mail_application), Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setAnchorView(R.id.fab).show()
            }
        }

        //-------------------------------------------------
        // Killされた場合に値を復帰する。
        // 一応ifで判定しているので!!.を使わなくてもビルドが通る。
        //-------------------------------------------------
        if (savedInstanceState != null) {
            app_state = savedInstanceState.getInt(APP_STATE_INDEX)
            startLocalDateTime = LocalDateTime.parse(savedInstanceState.getString(START_LOCAL_DATE_TIME_INDEX))
            finishLocalDateTime = LocalDateTime.parse(savedInstanceState.getString(FINISH_LOCAL_DATE_TIME_INDEX))
            elapsedLocalDateTime = savedInstanceState.getString(ELAPSED_LOCAL_DATE_TIME_INDEX).toString()
        }
    }

    /**
     * 履歴ファイルに履歴を追加する。
     *
     * @param string 履歴ファイルに追記する文字列。
     */
    fun appendHistory(string: String) {
        applicationContext.openFileOutput(FILE_NAME, Context.MODE_APPEND).bufferedWriter(Charsets.UTF_8).use { it.write(string) }
    }

    /**
     * 履歴ファイルを削除する。
     *
     * 履歴の削除を行う場合に、履歴ファイルを削除してファイルを再生成することでこの処理に代える。
     */
    fun clearHistory() {
        applicationContext.deleteFile(FILE_NAME)
        applicationContext.openFileOutput(FILE_NAME, Context.MODE_APPEND).bufferedWriter(Charsets.UTF_8).use { it.write("")}
    }

    /**
     * 履歴ファイルの内容を読み込む。
     *
     * @return 履歴ファイルから読み込んだ文字列。
     */
    fun readHistory(): String {
        return applicationContext.openFileInput(FILE_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    /**
     * 別のアクティビティが最前面になった場合の動作。
     *
     * 別のアクティビティが最前面になった場合、タイマーを停止する。
     * 停止したタイマーはレジューム時に再開する。
     */
    override fun onPause() {
        timer.cancel()
        super.onPause()
    }

    //----------------------------------------------------
    // 画面に表示する日時、および記録する日時を保持するメンバ変数。
    //----------------------------------------------------
    /** 開始日時を意味するメンバ変数。 */
    var startLocalDateTime: LocalDateTime = LocalDateTime.now()

    /** 終了日時を意味するメンバ変数。 */
    var finishLocalDateTime: LocalDateTime = startLocalDateTime

    /** 経過日時を意味するメンバ変数。この変数は終了日時から開始日時を減算した結果の文字列とする。*/
    var elapsedLocalDateTime: String = ""

    /**
     * アクティビティの初回起動時、および再開時の動作。
     *
     * アクティビティの初回起動時、および再開時にタイマーによる計測を開始・再開し、画面表示を更新する。
     */
    override fun onResume() {
        //----------------------------------------------
        // 経過時間を画面に表示するための別スレッドを定義する。
        //----------------------------------------------
        val runnable = object : Runnable {
            override fun run() {
                // NavHostFragmentの取得。
                // フラグメントの遷移はこのクラスが責任を持つらしい？
                val tempNavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment?

                // 現在表示されているフラグメントの取得する。
                // 取得できたフラグメントがFirstFragmentの場合、経過時間を計測中だと判断して画面の描画内容を更新する。
                if (tempNavHostFragment != null) {
                    val navHostFragment = tempNavHostFragment
                    val currentFragment = navHostFragment.childFragmentManager.fragments[0]

                    if (currentFragment is FirstFragment) {
                        val currentDateTime = LocalDateTime.now()
                        currentFragment.updateElapsedTime(currentDateTime)
                    }
                }
            }
        }

        //------------------------------
        // 1秒ごとにタイマーをカウントする。
        //------------------------------
        val timerTask = timerTask {
            handler.post(runnable)
        }
        timer = Timer()
        timer.schedule(timerTask, 0, INTERVAL)

        super.onResume()
    }

    //--------
    // killされた場合に値を保存する。
    //--------
    private var _task_title_bk: String = ""
    private val APP_STATE_INDEX: String = "APP_STATE_INDEX"
    private val START_LOCAL_DATE_TIME_INDEX: String = "START_LOCAL_DATE_TIME_INDEX"
    private val FINISH_LOCAL_DATE_TIME_INDEX: String = "FINISH_LOCAL_DATE_TIME_INDEX"
    private val ELAPSED_LOCAL_DATE_TIME_INDEX: String = "ELAPSED_LOCAL_DATE_TIME_INDEX"

    /**
     * Activity破棄時の動作。
     *
     * 以下の3つの値を保存する。この3つの値さえ取得できていれば、1秒後に元の状態を表示できる。
     *
     * - アプリの状態が「計測開始前」・「計測中」・「計測終了後」のいずれであるか。
     * - startLocalDateTimeの値
     * - finishLocalDateTimeの値
     * - elapsedLocalDateTimeの値
     *
     * もしかするとタスクのタイトルもバックアップが必要かもしれないが、値が勝手に復帰してくれているみたいなので対応は保留とする。
     *
     * - タスクのタイトル
     *
     * @param outState アクティビティのBundleオブジェクト？ なんだろう。
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(APP_STATE_INDEX, app_state)
        outState.putString(START_LOCAL_DATE_TIME_INDEX, startLocalDateTime.toString())
        outState.putString(FINISH_LOCAL_DATE_TIME_INDEX, finishLocalDateTime.toString())
        outState.putString(ELAPSED_LOCAL_DATE_TIME_INDEX, elapsedLocalDateTime.toString())
    }

    //------------------------------------------------------------------
    // 履歴を表示するフラグメント（SecondFragment）に関するメンバ変数・メソッド。
    //------------------------------------------------------------------
    /** 履歴を表示するフラグメント（SecondFragment）が描画済みであるかを示すフラグ。true: 描画済み、false: 未描画。 */
    private var isDrewSecondFragment = false

    /**
     * 履歴を表示するフラグメント（SecondFragment）が描画済みであるかを示すフラグを「未描画」にセットする関数。
     */
    fun setSecondFragmentFlagOff() {
        isDrewSecondFragment = false
    }

    /**
     * 履歴を表示するフラグメント（SecondFragment）が描画済みであるかを示すフラグを「描画済み」にセットする関数。
     */
    fun setSecondFragmentFlagOn() {
        isDrewSecondFragment = true
    }

    /**
     * ハンバーガーボタンの内容をセットする。
     *
     * ハンバーガーボタン押下時にはメニューを表示する。
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * メニュー選択時の動作。
     *
     * 選択可能なメニューは以下の通り。
     *   - 履歴を見る
     *   - 履歴を消す
     *
     * @param item 選択されたメニュー項目。
     * @return true:成功。false:失敗。
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        return when (item.itemId) {
            // 「履歴を見る」を選択した場合。
            R.id.action_view_history -> {
                // 履歴を表示するフラグメント（SecondFragment）を描画し、履歴を表示する。
                // すでにSecondFragmentを描画している場合にもメニューを選択できるので、
                // 描画済みの場合には何もしないように処理を分岐する。
                if (isDrewSecondFragment == true) {
                    Log.d(TAG, "Second Fragment was already drew.")
                } else {
                    Log.d(TAG, "Draw Second Fragment now.")
//                    // TODO:タスクタイトルのバックアップは不要かもしれない。
//                    _task_title_bk = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView_TaskTitle).text.toString()
//                    Log.d(TAG, "onOptionsItemSelected: ${_task_title_bk}")
                    navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
                }
                true
            }
            // 「履歴を消す」を選択した場合。
            R.id.action_delete_history -> {
                // Ok/Cancelボタンをもつダイアログを表示し、Ok選択時に履歴を削除する。
                val dialog = ConfirmDialog(
                    getString(R.string.confirm_delete_history),
                    getString(R.string.delete),
                    {
                        // 履歴を削除する（実際には履歴ファイルを削除し、再生成する。）
                        clearHistory()

                        // NavHostFragmentの取得
                        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

                        // 現在表示されているフラグメントを取得し、スナックバーに履歴を削除した旨を表示する。
                        val currentFragment = navHostFragment.childFragmentManager.fragments[0]
                        val view = currentFragment?.view as View
                        Snackbar.make(view, getString(R.string.deleting_history_completed), Snackbar.LENGTH_SHORT).setAnchorView(R.id.fab).show()
//                        // Toastを使う場合はこう。
//                        Toast.makeText(this, getString(R.string.deleting_history_completed), Toast.LENGTH_SHORT).show()
                    },
                    getString(R.string.cancel),
                    {
                        // 何もしない。
                    }
                )
                val manager = supportFragmentManager
                dialog.show(manager, "ConfirmDialog")
                true
            }
            // なんらかの要因で用意していないメニューが選択されたと判断された場合。
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * ソフトキーボードを隠す。
     *
     * 開いているソフトキーボードを隠す。
     * Enterボタンを入力してもソフトキーボードは閉じてくれないので、このような処理が別建てで必要になるようだ。
     */
    fun showOffSoftKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

//        // TODO: タスクタイトルのバックアップは不要かもしれない。
//        _task_title_bk = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView_TaskTitle).text.toString()
//        Log.d(TAG, "showOffSoftKeyboard: ${_task_title_bk}")
    }

    /**
     * テキストボックス以外の場所をタッチした時の動作。
     *
     * テキストボックス以外をタッチした際にはソフトキーボードを隠す。
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        showOffSoftKeyboard()
        return super.onTouchEvent(event)
    }

    /**
     * Viewをタッチした時のリスナー。
     *
     * こちらもテキストボックス以外をタッチした時の動作で、テキストボックスからフォーカスを外す。
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        binding.root.requestFocus()
        return super.dispatchTouchEvent(ev)
    }

    /**
     * アクションバーの戻るボタン押下時の動作。
     *
     * アクションバーで戻るボタンを押下した場合、ナビゲーションコントローラーの記述に従い画面を戻す。
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}