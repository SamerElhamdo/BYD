import android.os.IBinder;
import android.os.Parcel;
import android.os.Binder;
import java.lang.reflect.Method;

/**
 * Minimal raw Binder client for the car's native "autoservice" service
 * (android.gui.BYDAutoServer). Runs standalone via app_process — no app,
 * no BYD SDK classes, no reflection on BYD code. Only public
 * android.os.{Parcel,IBinder,Binder} APIs, plus one reflective lookup of
 * the hidden android.os.ServiceManager.getService(String), the same
 * technique already used for ReadTest.java / HelperDaemonMain.
 *
 * Transaction codes 1-20 and their write/read Parcel shapes are documented
 * in byd-seal05-research-plan.md section 8.11. Codes confirmed by live
 * testing are in section 8.13.
 *
 * Usage:
 *   AutoServiceClient <code> [arg ...]
 * where each arg is one of:
 *   i32:<n>      write a 32-bit int
 *   binder       write a fresh throwaway android.os.Binder() as a StrongBinder
 *
 * Example (confirmed getInt, code 5, device=1001, fid=driver window %):
 *   AutoServiceClient 5 i32:1001 i32:947912728
 *
 * This tool only ever does what you tell it — it does not know which
 * codes are destructive. Treat any call whose arg list looks like
 * (device, fid, value, binder) as a potential WRITE until proven
 * otherwise by a confirming read before and after.
 */
public class AutoServiceClient {
    private static final String DESCRIPTOR = "android.gui.BYDAutoServer";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: AutoServiceClient <code> [i32:<n> | binder] ...");
            return;
        }

        int code;
        try {
            code = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("FAILED: first argument must be the numeric transaction code");
            return;
        }

        int intArgCount = 0;
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("i32:")) intArgCount++;
        }
        if (intArgCount >= 3) {
            System.out.println("NOTE: " + intArgCount + " int args supplied — this shape matches a"
                    + " possible WRITE call per section 8.11. Confirm this is intentional.");
        }

        IBinder remote;
        try {
            Class<?> smClass = Class.forName("android.os.ServiceManager");
            Method getService = smClass.getMethod("getService", String.class);
            remote = (IBinder) getService.invoke(null, "autoservice");
        } catch (Throwable t) {
            System.out.println("FAILED to look up autoservice: " + t);
            return;
        }
        if (remote == null) {
            System.out.println("FAILED: ServiceManager.getService(\"autoservice\") returned null");
            return;
        }

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("i32:")) {
                    data.writeInt(Integer.parseInt(arg.substring(4)));
                } else if (arg.equals("binder")) {
                    data.writeStrongBinder(new Binder());
                } else {
                    System.out.println("FAILED: unknown arg '" + arg + "' (use i32:<n> or binder)");
                    return;
                }
            }

            boolean ok;
            try {
                ok = remote.transact(code, data, reply, 0);
            } catch (Throwable t) {
                System.out.println("transact() THREW: " + t);
                return;
            }
            System.out.println("transact() returned: " + ok);

            int avail = reply.dataAvail();
            System.out.println("reply bytes available: " + avail);
            int pos = reply.dataPosition();
            while (reply.dataAvail() >= 4) {
                int before = reply.dataPosition();
                int value = reply.readInt();
                System.out.println("  [+" + (before - pos) + "] int32 = " + value
                        + "  (0x" + Integer.toHexString(value) + ")");
            }
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}
