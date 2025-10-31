package com.bea.nutria.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String[] MENSAGENS = {
            "O app te mandou um aceno educado. Nenhum dado foi ferido nesse processo.",
            "Nada urgente. Só testando se você ainda lembra a senha.",
            "Silêncio é bom. Mas interação é melhor.",
            "A IA ficou reflexiva e escreveu isso pra você. Não pergunte o motivo.",
            "Aviso importante: este é um aviso sem importância."
    };

    private static int index = 0;

    @Override
    public void onReceive(Context context, Intent intent) {

        String msg = MENSAGENS[index];
        NotificationHelper.showNotification(context, "Nutria notifica!!", msg, 2000 + index);

        index = (index + 1) % MENSAGENS.length;
        NotificationScheduler.scheduleNext(context, NotificationScheduler.INTERVAL_MS);
    }
}
