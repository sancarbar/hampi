package ui.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.support.annotation.ArrayRes
import android.support.annotation.StringRes
import co.sancarbar.hampi.R

/**
 * @author Santiago Carrillo
 * 7/24/18.
 */
class DialogFactory {

    companion object {

        fun showInfoDialog(context: Context, @StringRes title: Int, @StringRes message: Int) {
            val builder = AlertDialog.Builder(context)
            val alertDialog = builder.create()
            alertDialog.setTitle(context.getString(title))
            alertDialog.setMessage(context.getString(message))
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getText(R.string.OK)
            ) { dialog, which -> dialog.dismiss() }
            alertDialog.show()
        }

        fun showSingleChoiceDialog(context: Context, @ArrayRes items: Int, @StringRes title: Int, listener: DialogInterface.OnClickListener) {
            val builder = AlertDialog.Builder(context)
            builder.setSingleChoiceItems(items, -1, listener)
            val alertDialog = builder.create()
            alertDialog.setTitle(context.getString(title))
            alertDialog.show()
        }
    }
}