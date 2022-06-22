package Demo.Tool;

import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Component;

@Component
public class DeviceUtil {
    public static String getdevice(Device device){
        if (device.isMobile()) {
            return "手机";
        } else if (device.isTablet()) {
            return "平板";
        } else if(device.isNormal()){
            return "PC";
        }else {
            return "未知";
        }
    }
}
