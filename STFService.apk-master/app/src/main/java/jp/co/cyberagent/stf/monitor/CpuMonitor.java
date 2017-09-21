package jp.co.cyberagent.stf.monitor;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import jp.co.cyberagent.stf.io.MessageWritable;
import jp.co.cyberagent.stf.proto.Wire;

import static android.content.Context.ACTIVITY_SERVICE;

public class CpuMonitor extends AbstractMonitor {
    private static final String TAG  = "STFCpuMonitor";

    private CpuState state = null;
    private int cpu_usage = 0;
    private String pkg =null;
    private int[] ram_usage = new int[2];

    public CpuMonitor(Context context, MessageWritable writer) {
        super(context, writer);
    }

    @Override
    public void run() {
        Log.i(TAG, "CPU Monitor starting");
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.i(TAG, "Action Package Added detected in CPU Monitor");
                String action = intent.getAction();
                pkg = intent.getData().getEncodedSchemeSpecificPart();
                Log.i(TAG, String.format("Package %s was added", pkg));

                Timer timer = new Timer();
                TimerTask doBroadcast = new TimerTask() {
                    @Override
                    public void run() {
                        cpu_usage = getCpuUsage(pkg);
                        //pss on 0th index, uss on 1st index
                        ram_usage = getRamUsage(pkg);
                        state = new CpuState(cpu_usage,ram_usage);
                        report(writer, state);
                    }
                };
                timer.schedule(doBroadcast, 0, 500);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        context.registerReceiver(receiver, intentFilter);

        try {
            synchronized (this) {
                while (!isInterrupted()) {
                    wait();
                }
            }
        }
        catch (InterruptedException e) {

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            Log.i(TAG, "Monitor stopping");
            context.unregisterReceiver(receiver);
        }
    }

    @Override
    public void peek(MessageWritable writer) {
        if (state != null) {
            report(writer, state);
        }
    }

    private void report(MessageWritable writer, CpuState state) {

        writer.write(Wire.Envelope.newBuilder()
                .setType(Wire.MessageType.EVENT_CPU)
                .setMessage(Wire.CpuEvent.newBuilder()
                        .setCpuConsumed(state.cpu_consumed)
                        .setMemShared(state.mem_shared)
                        .setMemStandalone(state.mem_standalone)
                        .build()
                        .toByteString())
                .build());

    }

    private static class CpuState {

        private int cpu_consumed;
        private int mem_shared;
        private int mem_standalone;

        public CpuState(int cpu_val, int[] ram_val){

            cpu_consumed = cpu_val;
            mem_shared = ram_val[0];
            mem_standalone = ram_val[1];

        }
    }

    public int[] getRamUsage(String pkgName) {

        int pss = 0;
        int uss = 0;
        int[] memory_component = new int[2];

        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        Map<Integer, String> pidMap = new TreeMap<Integer, String>();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses)
        {
            pidMap.put(runningAppProcessInfo.pid, runningAppProcessInfo.processName);
        }
        Collection<Integer> keys = pidMap.keySet();
        for(int key : keys)
        {
            int pids[] = new int[1];
            pids[0] = key;
            android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);
            for(android.os.Debug.MemoryInfo pidMemoryInfo: memoryInfoArray)
            if (pidMap.get(pids[0]).equals(pkgName))
                {
                    pss = pidMemoryInfo.getTotalPss();
                    memory_component[0] = pss;
                    uss = pidMemoryInfo.getTotalPrivateDirty();
                    memory_component[1] = uss;
                    //Log.i(TAG, String.format("** MEMINFO in pid %d [%s] **\n",pids[0],pidMap.get(pids[0])));
                    //Log.i(TAG, " pidMemoryInfo.getTotalPrivateDirty(): " + pidMemoryInfo.getTotalPrivateDirty() + "\n");
                    //Log.i(TAG, " pidMemoryInfo.getTotalPss(): " + pidMemoryInfo.getTotalPss() + "\n");
                    //Log.i(TAG, " pidMemoryInfo.getTotalSharedDirty(): " + pidMemoryInfo.getTotalSharedDirty() + "\n");
                }
        }
        return memory_component;
    }

    public int getCpuUsage(String pkgname){

        String parameters = null;
        int cpuUsage = 0;

        try {
            String cmd[] = { "/system/bin/sh", "-c", "top -n 1 | grep "+ pkgname };
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((parameters = in.readLine()) != null) {
                parameters = parameters.trim();
                //Log.i(TAG, parameters);
                String[] tokens = parameters.split(" +");
                cpuUsage = Integer.parseInt(tokens[2].replaceAll("%+$", ""));
                //Log.i(TAG, Integer.toString(cpuUsage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cpuUsage;
    }
}