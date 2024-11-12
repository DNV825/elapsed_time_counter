package io.github.dnv825.elapsedtimecounter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import io.github.dnv825.elapsedtimecounter.databinding.FragmentFirstBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 第1フラグメントクラス
 *
 * デフォルト生成された1つ目のフラグメント。
 * > A simple [Fragment] subclass as the default destination in the navigation.
 * このフラグメントに各種項目を表示する。
 */
class FirstFragment : Fragment() {

    //---------------------------------------
    // デフォルト生成されたBinding系のメンバ変数。
    //---------------------------------------
    /** （デフォルト生成されたメンバ変数。） **/
    private var _binding: FragmentFirstBinding? = null

    /**
     * （デフォルト生成されたメンバ変数。）
     * > // This property is only valid between onCreateView and
     * > // onDestroyView.
     */
    private val binding get() = _binding!!

    //------------------
    // Log.d()用の定数。
    //------------------
    /** Log.d()の第1引数へ指定するクラス名。 */
    private val TAG = FirstFragment::class.java.getSimpleName()

    /**
     * フラグメントが初めてUIを描画する際の動作。
     *
     * フラグメントのビューを作成し、戻り値として返す。
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View（Viewってなんだ？）
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * フラグメントのビューが生成された後に呼び出される動作。
     *
     * ビューの初期化とフラグメント状態の復元を行う。
     *
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val maActivity = activity as MainActivity?

        // タスク名入力後にエンターを押下した際の動作。ソフトウェアキーボードを閉じる。
        binding.autoCompleteTextViewTaskTitle.setOnEditorActionListener { _, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH ||
                i == EditorInfo.IME_ACTION_DONE ||
                keyEvent != null &&
                keyEvent.action == KeyEvent.ACTION_DOWN &&
                keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                if (keyEvent == null || !keyEvent.isShiftPressed) {
                    maActivity?.showOffSoftKeyboard() // キーボード非表示
                    return@setOnEditorActionListener true
                } else {
                    return@setOnEditorActionListener false
                }
            } else {
                return@setOnEditorActionListener false
            }
        }

        // AutoCompleteTextViewに候補を表示する。
        val adapter = ArrayAdapter<String>(requireContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item, maActivity!!.taskTitleHistoryOptions)
        binding.autoCompleteTextViewTaskTitle.setAdapter(adapter)
        binding.autoCompleteTextViewTaskTitle.setThreshold(0); // 0文字入力すると候補を表示する設定だが、これだけではフォーカス時に候補が表示されない。

        // AutoCompleteTextViewにフォーカスした際にドロップダウンを表示する。
        binding.autoCompleteTextViewTaskTitle.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.autoCompleteTextViewTaskTitle.showDropDown()
            }
        }

        // PLAYボタン・STOPボタン・REFRESHボタン押下時の動作。
        binding.imageButtonRun.setOnClickListener {
            val app_state = maActivity?.getAppState()

            if (app_state == maActivity?.PLAY_STATE) {
                // アプリの状態を STOP_STATE にしてボタンを更新する。
                maActivity?.setAppState(maActivity.STOP_STATE)
                binding.imageButtonRun.setImageResource(R.drawable.baseline_stop_24)
            } else if (app_state == maActivity?.STOP_STATE) {
                // アプリの状態を REFRESH_STATE にしてボタンを更新する。
                maActivity?.setAppState(maActivity.REFRESH_STATE)
                binding.imageButtonRun.setImageResource(R.drawable.baseline_refresh_24)

                // 結果をファイルへ出力する。
                val history = "${binding.textViewStartDate.text} ${binding.textViewStartTime.text}\t${binding.textViewFinishDate.text} ${binding.textViewFinishTime.text}\t${binding.textViewElapsedTime.text}\t${binding.autoCompleteTextViewTaskTitle.text}\n"
                maActivity?.appendHistory(history)

                // タイトルの履歴（TaskTitleHistoryOptions）を更新する。
                maActivity?.updateTaskTitleHistoryOptions(binding.autoCompleteTextViewTaskTitle.text.toString(), maActivity!!.finishLocalDateTime)
                val adapter = ArrayAdapter<String>(requireContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item, maActivity!!.taskTitleHistoryOptions)
                binding.autoCompleteTextViewTaskTitle.setAdapter(adapter)
                binding.autoCompleteTextViewTaskTitle.setThreshold(0); // 0文字入力すると候補を表示する設定だが、これだけではフォーカス時に候補が表示されない。

                Log.d(TAG, history)
            } else {
                // アプリの状態を PLAY_STATE にしてボタンを更新する。
                maActivity?.setAppState(maActivity.PLAY_STATE)
                binding.imageButtonRun.setImageResource(R.drawable.baseline_play_arrow_24)
            }
        }

//        // 次に進むボタンを押下した場合の動作。デフォルト生成されたもの。
//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }
    }

    //----------------------------------------------------------------------------------
    // 日付・時刻の表示フォーマットの定義。
    // 一応「ISO 8601」に従うが、表示に使うのはローカル時刻であり、かつ表示として見やすく成型するので
    // 完全に「ISO 8601」に従うわけではない。
    //----------------------------------------------------------------------------------
    /** 年月日のフォーマット。一応「ISO 8601」に従うが、日の後ろに"T"はつけない。 */
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** 時分秒のフォーマット。一応「ISO 8601」に従うが、タイムゾーンは付与しない。 */
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    /**
     * 経過時刻の表示を更新する。
     *
     * Fragmentに表示している各時刻の表示を以下のルールで更新する。
     *
     * - 「計測開始前」の場合：startの時刻を1秒ごとに更新する。elapsedの表示は 00:00:00 にする。
     * - 「計測中」の場合：startの時刻を計測開始時の時刻で固定する。finishとelapsedの時刻を1秒ごとに更新する。
     * - 「計測終了後」の場合：finish・elapsedを計測終了時の時刻で固定する。表示は更新しない。
     *
     * @param currentLocalDateTime 現在時刻。
     */
    fun updateElapsedTime(currentLocalDateTime: LocalDateTime) {
        val maActivity = activity as MainActivity?
        val app_state = maActivity?.getAppState()

        if (app_state == maActivity?.PLAY_STATE) {
            // アプリの状態が PLAY_STATE の場合、
            //   start:   1秒ごとに表示を更新する。
            //   finish:  空白にする。その後は更新しない。
            //   elapsed: 00:00:00 にする。その後は更新しない。
            maActivity?.startLocalDateTime = currentLocalDateTime

            // start.
            binding.textViewStartDate.text = maActivity?.startLocalDateTime?.format(dateFormatter)
            binding.textViewStartTime.text = maActivity?.startLocalDateTime?.format(timeFormatter)

            // finish.
            if (binding.textViewFinishDate.text != "") {
                binding.textViewFinishDate.text = ""
                binding.textViewFinishTime.text = ""
            }

            // elapsed.
            if (binding.textViewElapsedTime.text != getString(R.string.before_count)) {
                binding.textViewElapsedTime.text = getString(R.string.before_count)
            }
        } else if (app_state == maActivity?.STOP_STATE) {
            // アプリの状態が PLAY_STATE の場合、
            //   start:   開始時の時刻で固定する。その後は更新しない。
            //   finish:  1秒ごとに表示を更新する。
            //   elapsed: 1秒ごとに表示を更新する。通常は hh:mm:ss、1日を超えたら dd:hh:mm:ss。
            maActivity?.finishLocalDateTime = currentLocalDateTime

            // start.
            // 特に何もしない。それで固定された状態になる。

            // finish.
            binding.textViewFinishDate.text = maActivity?.finishLocalDateTime?.format(dateFormatter)
            binding.textViewFinishTime.text = maActivity?.finishLocalDateTime?.format(timeFormatter)

            // elapsed.
            val elapsed_seconds = ChronoUnit.SECONDS.between(maActivity?.startLocalDateTime, maActivity?.finishLocalDateTime)
            val s = elapsed_seconds % 60
            val m = (elapsed_seconds / 60) % 60
            val h = (elapsed_seconds / 60 / 60) % 24
            val d = (elapsed_seconds / 60 / 60 / 24)
            Log.d(FirstFragment::class.java.getSimpleName(),"d:${d}, h:${h}, m:${m}, s:${s}")

            if (d == 0L) {
                // 1日経過していない場合。
                maActivity?.elapsedLocalDateTime = "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
            } else {
                // 1日以上経過している場合。
                maActivity?.elapsedLocalDateTime = "${d.toString().padStart(2, '0')}:${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
            }

            binding.textViewElapsedTime.text = maActivity?.elapsedLocalDateTime
        } else {
            // アプリの状態が REFRESH_STATE の場合、
            //   start:   開始時の時刻で固定する。その後は更新しない。
            //   finish:  終了時の時刻で固定する。その後は更新しない。
            //   elapsed: 終了時の計測時間で固定する。その後は更新しない。
            // 何もしなくてよい。
        }
    }

    /**
     * フラグメント再開時の動作。
     *
     * フラグメント再開時は停止時の表示を復帰する。
     * 復帰後1秒経過すると画面表示が更新され、正常な表示に切り替わる。
     */
    override fun onResume() {
        super.onResume()
        val maActivity = activity as MainActivity?
        val app_state = maActivity?.getAppState()

        if (app_state == maActivity?.PLAY_STATE) {
            // アプリの状態が PLAY_STATE の場合、
            //   start:   1秒ごとに表示を更新する。
            //   finish:  空白にする。その後は更新しない。
            //   elapsed: 00:00:00 にする。その後は更新しない。
            binding.textViewStartDate.text = maActivity?.startLocalDateTime?.format(dateFormatter)
            binding.textViewStartTime.text = maActivity?.startLocalDateTime?.format(timeFormatter)

            binding.textViewFinishDate.text = ""
            binding.textViewFinishTime.text = ""

            binding.textViewElapsedTime.text = "00:00:00"

            binding.imageButtonRun.setImageResource(R.drawable.baseline_play_arrow_24)
        } else if (app_state == maActivity?.STOP_STATE) {
            // アプリの状態が PLAY_STATE の場合、
            //   start:   開始時の時刻で固定する。その後は更新しない。
            //   finish:  1秒ごとに表示を更新する。
            //   elapsed: 1秒ごとに表示を更新する。通常は hh:mm:ss、1日を超えたら dd:hh:mm:ss。
            binding.textViewStartDate.text = maActivity?.startLocalDateTime?.format(dateFormatter)
            binding.textViewStartTime.text = maActivity?.startLocalDateTime?.format(timeFormatter)

            binding.textViewFinishDate.text = maActivity?.finishLocalDateTime?.format(dateFormatter)
            binding.textViewFinishTime.text = maActivity?.finishLocalDateTime?.format(timeFormatter)

            binding.textViewElapsedTime.text = maActivity?.elapsedLocalDateTime

            binding.imageButtonRun.setImageResource(R.drawable.baseline_stop_24)
        } else {
            // アプリの状態が REFRESH_STATE の場合、
            //   start:   開始時の時刻で固定する。その後は更新しない。
            //   finish:  終了時の時刻で固定する。その後は更新しない。
            //   elapsed: 終了時の計測時間で固定する。その後は更新しない。
            binding.textViewStartDate.text = maActivity?.startLocalDateTime?.format(dateFormatter)
            binding.textViewStartTime.text = maActivity?.startLocalDateTime?.format(timeFormatter)

            binding.textViewFinishDate.text = maActivity?.finishLocalDateTime?.format(dateFormatter)
            binding.textViewFinishTime.text = maActivity?.finishLocalDateTime?.format(timeFormatter)

            binding.textViewElapsedTime.text = maActivity?.elapsedLocalDateTime

            binding.imageButtonRun.setImageResource(R.drawable.baseline_refresh_24)
        }
    }

    /**
     * ビュー破棄時の動作。
     *
     * デフォルト生成された関数。
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}