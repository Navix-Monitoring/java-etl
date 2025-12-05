package school.sptech;

import java.util.HashMap;
import java.util.Map;

public class Mapeamento {

    public static Map<String, String> criarMapeamento() {
        Map<String, String> map = new HashMap<>();

        // ================================
        // INTERFACE, TELA E PAINEL DO CARRO
        // ================================
        map.put("Xorg", "Painel de Informações do Carro");
        map.put("wayland", "Sistema de Exibição do Painel");
        map.put("gnome-shell", "Interface de Condução Autônoma");
        map.put("kwin", "Controle Visual do Painel");
        map.put("plasmashell", "Painel Central do Veículo");
        map.put("gdm", "Tela de Acesso do Motorista");
        map.put("lightdm", "Entrada do Motorista");
        map.put("sddm", "Controle de Abertura do Veículo");

        // ================================
        // SISTEMA OPERACIONAL DO CARRO
        // ================================
        map.put("systemd", "Núcleo do Veículo");
        map.put("systemd-logind", "Controle de Presença do Motorista");
        map.put("systemd-journald", "Registro de Eventos do Carro");
        map.put("systemd-udevd", "Ativador de Sensores do Veículo");
        map.put("NetworkManager", "Gerenciador de Conexões do Carro");
        map.put("polkitd", "Verificador de Permissões");
        map.put("cron", "Agendador de Rotinas");
        map.put("atd", "Programador de Tarefas");
        map.put("modprobe", "Carregador de Funções Internas");

        // ================================
        // PROCESSOS INTERNOS / MOTOR DIGITAL
        // ================================
        map.put("ksoftirqd", "Resposta Rápida de Sensores");
        map.put("kworker", "Operador Interno do Sistema");
        map.put("rcu_preempt", "Sincronização Interna");
        map.put("ksmd", "Otimizador de Memória");
        map.put("khungtaskd", "Monitor de Travamentos");
        map.put("watchdog", "Guardião de Segurança");

        // ================================
        // REDE, INTERNET E COMUNICAÇÃO
        // ================================
        map.put("wpa_supplicant", "Conexão Wi-Fi do Carro");
        map.put("nm-applet", "Indicador de Rede");
        map.put("bluetoothd", "Comunicação de Curta Distância");
        map.put("dhclient", "Solicitação de Endereço de Rede");
        map.put("dnsmasq", "Cache de Navegação");
        map.put("ssh", "Acesso Remoto de Diagnóstico");
        map.put("cups-browsed", "Localizador de Dispositivos");

        // ================================
        // HARDWARE / SENSORES E COMPONENTES
        // ================================
        map.put("pulseaudio", "Controle de Áudio Interno");
        map.put("pipewire", "Central Multimídia");
        map.put("upowerd", "Gerenciador de Energia");
        map.put("udisksd", "Controle de Armazenamento");
        map.put("fwupd", "Atualização de Componentes");
        map.put("boltd", "Porta de Alta Velocidade");
        map.put("i915", "Motor Gráfico do Painel");
        map.put("rtw88", "Sensor de Conexão Sem Fio");

        // ================================
        // SEGURANÇA E PROTEÇÃO DO SISTEMA
        // ================================
        map.put("apparmor", "Sistema de Proteção do Veículo");
        map.put("selinuxd", "Monitor de Integridade");
        map.put("gnome-keyring-daemon", "Cofre de Senhas");
        map.put("clamd", "Varredor de Ameaças");

        // ================================
        // APLICATIVOS HUMANIZADOS
        // ================================
        map.put("firefox", "Navegador");
        map.put("chrome", "Navegador de Internet");
        map.put("spotify", "Música do Carro");
        map.put("discord", "Comunicação por Voz");
        map.put("steam", "Área de Entretenimento");
        map.put("code", "Ferramenta do Programador");
        map.put("python3", "Scripts de Análise");
        map.put("java", "Central de Controle");
        map.put("teams", "Comunicação Corporativa");
        map.put("slack", "Mensagens de Trabalho");

        // ================================
        // BANCOS DE DADOS E ARMAZENAMENTO
        // ================================
        map.put("mysqld", "Banco de Dados de Navegação");
        map.put("postgres", "Armazenamento de Rotas");
        map.put("mongod", "Registro de Telemetria");
        map.put("redis-server", "Cache de Dados Rápidos");

        return map;
    }
}

