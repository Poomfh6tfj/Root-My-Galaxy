#include <jni.h>

#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>

static void append_probe(char *output, size_t output_size, const char *name,
                         const char *path, int flags) {
  errno = 0;
  int fd = open(path, flags | O_CLOEXEC);
  int saved_errno = errno;
  if (fd >= 0) {
    close(fd);
  }

  size_t used = strlen(output);
  if (used >= output_size) {
    return;
  }
  snprintf(output + used, output_size - used, "%s=%s errno=%d\n", name,
           fd >= 0 ? "ok" : "denied", saved_errno);
}

static int read_line(const char *path, char *output, size_t output_size) {
  int fd = open(path, O_RDONLY | O_CLOEXEC);
  if (fd < 0) {
    return 0;
  }
  ssize_t count = read(fd, output, output_size - 1);
  close(fd);
  if (count <= 0) {
    return 0;
  }
  output[count] = '\0';
  output[strcspn(output, "\r\n")] = '\0';
  return 1;
}

JNIEXPORT jstring JNICALL
Java_dev_busung_s25uroot_NativeProbe_run(JNIEnv *env, jobject thiz) {
  (void)thiz;
  char output[2048] = {0};
  char context[256] = "unknown";
  int context_fd = open("/proc/self/attr/current", O_RDONLY | O_CLOEXEC);
  if (context_fd >= 0) {
    ssize_t count = read(context_fd, context, sizeof(context) - 1);
    close(context_fd);
    if (count > 0) {
      context[count] = '\0';
      context[strcspn(context, "\r\n")] = '\0';
    }
  }

  snprintf(output, sizeof(output),
           "uid=%u euid=%u gid=%u egid=%u\ncontext=%s\npage_size=%ld\n",
           getuid(), geteuid(), getgid(), getegid(), context,
           sysconf(_SC_PAGESIZE));
  append_probe(output, sizeof(output), "tracefs_control",
               "/sys/kernel/tracing/tracing_on", O_RDWR);
  append_probe(output, sizeof(output), "tracefs_event",
               "/sys/kernel/tracing/events/workqueue/workqueue_execute_start/enable",
               O_RDWR);
  append_probe(output, sizeof(output), "tracefs_pipe",
               "/sys/kernel/tracing/per_cpu/cpu0/trace_pipe_raw", O_RDONLY);
  append_probe(output, sizeof(output), "ashmem", "/dev/ashmem", O_RDWR);
  char boot_id[64] = {0};
  if (read_line("/proc/sys/kernel/random/boot_id", boot_id,
                sizeof(boot_id))) {
    char ashmem_libcutils[128];
    snprintf(ashmem_libcutils, sizeof(ashmem_libcutils), "/dev/ashmem%s",
             boot_id);
    append_probe(output, sizeof(output), "ashmem_libcutils",
                 ashmem_libcutils, O_RDWR);
  }
  append_probe(output, sizeof(output), "slabinfo", "/proc/slabinfo", O_RDONLY);
  append_probe(output, sizeof(output), "boot_id",
               "/proc/sys/kernel/random/boot_id", O_RDONLY);
  append_probe(output, sizeof(output), "proc_self_mem", "/proc/self/mem",
               O_RDONLY);

  return (*env)->NewStringUTF(env, output);
}
