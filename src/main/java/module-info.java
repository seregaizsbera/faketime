module ru.seregaizsbera.faketime {
    requires java.instrument;
    requires net.bytebuddy;
    requires net.bytebuddy.agent;
    requires static ru.seregaizsbera.faketime.interceptors;
    exports ru.seregaizsbera.faketime;
}
