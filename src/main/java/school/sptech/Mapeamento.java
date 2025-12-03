package school.sptech;

import java.util.HashMap;
import java.util.Map;

public class Mapeamento {
    public static Map<String, String> criarMapeamento(){
        Map<String, String> map = new HashMap<>();
        map.put("mysqld", "SensorFusion_DB_Manager");
        map.put("Xorg", "Vehicle_HMI_Display_Driver");
        map.put("gnome-shell", "Autonomous_Navigation_System");
        map.put("chrome", "Browser_Module");

        map.put("dbus-daemon", "InterProcess_Comms_Bus");
        map.put("at-spi2-registryd", "Accessibility_Feature_Service");
        map.put("xdg-desktop-portal", "System_Integration_Layer");
        map.put("xdg-desktop-portal-gtk", "System_Integration_Layer_GTK");
        map.put("containerd", "Containerized_Environments_Mgr"); // Se usar containers
        map.put("avahi-daemon", "Network_Discovery_Service");
        map.put("thermald", "Thermal_Management_Unit_Driver");

        map.put("code", "Artificial_Inteligence_Constructing");
        map.put("idea", "Artificial_Inteligence_Translating");
        map.put("python", "Artificial_Inteligence_Mapping");
        map.put("java", "Main_Control_Software");

        map.put("rcu_sched", "Kernel_Scheduler_RCU");
        map.put("migration/1", "Kernel_Core_Migration_Handler");
        map.put("kworker/1:0-events", "Kernel_Event_Worker_Core1");
        map.put("kswapd0", "Memory_Management_Unit");
        map.put("irq/135-VEN_04F", "HW_Interrupt_Driver_A");
        map.put("kworker/u16:8-events_unbound", "Kernel_Event_Worker_Unbound");
        map.put("irq/140-rtw88_p", "HW_Interrupt_Driver_Wifi_Sensor");
        map.put("kworker/3:4-events", "Kernel_Event_Worker_Core3");
        map.put("kworker/u16:1-i915", "GPU_Display_Driver_Kernel_I915");
        map.put("kworker/u17:1-i915_flip", "GPU_Buffer_Flip_Handler");
        map.put("kworker/0:2-mm_percpu_wq", "Kernel_Memory_Worker");

        return map;
    }
}