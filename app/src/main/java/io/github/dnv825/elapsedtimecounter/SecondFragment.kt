package io.github.dnv825.elapsedtimecounter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.dnv825.elapsedtimecounter.databinding.FragmentSecondBinding

/**
 * 第2フラグメントクラス。
 *
 * デフォルト生成された2つ目のフラグメント。
 * > A simple [Fragment] subclass as the second destination in the navigation.
 * このフラグメントには記録した履歴を表示する。
 */
class SecondFragment : Fragment() {

    //---------------------------------------
    // デフォルト生成されたBinding系のメンバ変数。
    //---------------------------------------
    /** （デフォルト生成されたメンバ変数。） **/
    private var _binding: FragmentSecondBinding? = null

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
    private val TAG = SecondFragment::class.java.getSimpleName()

    /**
     * フラグメントが初めてUIを描画する際の動作。
     *
     * フラグメントのビューを作成し、戻り値として返す。
     * SecondFragmentはメニュー選択すると描画を行うので、すでに描画されている場合に再描画しないようにするため
     * MainActivityのメンバ変数「isDrewSecondFragment」に描画状態を記録する。
     * このメソッドが呼び出された場合、フラグメントが描画されたものとしてtrueをセットする。
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View（Viewってなんだ？）
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        // isDrewSecondFragmentをtrueにセットする。
        val maActivity = activity as MainActivity?
        maActivity?.setSecondFragmentFlagOn()

        return binding.root

    }

    /**
     * フラグメントのビューが生成された後に呼び出される動作。
     *
     * ビューの初期化とフラグメント状態の復元を行う。
     * この段階で履歴ファイルの内容を読み込み、文字列として描画する。
     *
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val maActivity = activity as MainActivity?

        // ファイルから履歴を読み込み表示する。
        val history = maActivity?.readHistory()
        val edited_history = history?.replace("\n", "\n\n")?.replace("\t", "\n")
        binding.textviewSecond.text = edited_history
        Log.d(TAG, history.toString())

//        // 次に進むボタンを押下した場合の動作。デフォルト生成されたもの。
//        binding.buttonSecond.setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
//        }
    }

    /**
     * ビュー破棄時の動作。
     *
     * デフォルト生成された関数。
     * SecondFragmentはメニュー選択すると描画を行うので、すでに描画されている場合に再描画しないようにするため
     * MainActivityのメンバ変数「isDrewSecondFragment」に描画状態を記録する。
     * このメソッドが呼び出された場合、フラグメントの描画が終了したものとしてfalseをセットする。
     */
    override fun onDestroyView() {
        // デフォルトの終了処理。
        super.onDestroyView()
        _binding = null

        // isDrewSecondFragmentをfalseにセットする。
        val maActivity = activity as MainActivity?
        maActivity?.setSecondFragmentFlagOff()
    }
}