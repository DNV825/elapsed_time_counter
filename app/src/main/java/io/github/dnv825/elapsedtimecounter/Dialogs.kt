package io.github.dnv825.elapsedtimecounter

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

/**
 * 確認用ダイアログクラス。
 *
 * Ok/Cancelボタンを持つダイアログのクラス。
 *
 * @param okLabel Okボタン上に表示する文字列。
 * @param okSelected Okボタン選択時に動作させる関数。
 * @param cancelLabel Cancelボタン上に表示する文字列。
 * @param cancelSelected Cancelボタン選択時に動作させる関数。
 */
class ConfirmDialog(private val message: String,
    private val okLabel: String,
    private val okSelected: () -> Unit,
    private val cancelLabel: String,
    private val cancelSelected: () -> Unit
): DialogFragment() {

    /**
     * ダイアログを生成する。
     *
     * Ok/Cancelボタンを持つダイアログを生成する。
     *
     * @param savedInstanceState （自動生成されたので使い道はよくわからない。）
     * @return コンストラクタの引数を反映したDialogクラス。
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(message)
        builder.setPositiveButton(okLabel) { dialog, which -> okSelected() }
        builder.setNegativeButton(cancelLabel) { dialog, which -> cancelSelected() }

//        return super.onCreateDialog(savedInstanceState)
        return builder.create()
    }
}

/**
 * Okダイアログクラス。
 *
 * Okボタンを持つダイアログのクラス。
 *
 * @param okLabel Okボタン上に表示する文字列。
 * @param okSelected Okボタン選択時に動作させる関数。
 */
class OkDialog(private val message: String,
                    private val okLabel: String,
                    private val okSelected: () -> Unit,
): DialogFragment() {

    /**
     * ダイアログを生成する。
     *
     * Ok/Cancelボタンを持つダイアログを生成する。
     *
     * @param savedInstanceState （自動生成されたので使い道はよくわからない。）
     * @return コンストラクタの引数を反映したDialogクラス。
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(message)
        builder.setPositiveButton(okLabel) { dialog, which -> okSelected() }

//        return super.onCreateDialog(savedInstanceState)
        return builder.create()
    }
}