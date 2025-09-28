Telemetry Lab — Mini-assignment
What this project contains - Kotlin + Jetpack Compose app named Telemetry Lab. -
ForegroundService ( TelemetryService ) that runs a CPU-bound 2D convolution at 20 Hz (or reduced
frequency when Battery Saver is ON). - UI to Start/Stop, slider for Compute Load (1–5), live dashboard
for frame latency & moving average, jank% logging instructions, and a continuously scrolling counter
for visible UI work. - Backpressure implemented via a Channel of capacity 2 (drop oldest) so work
never queues indefinitely. - Coroutine-based off-main-thread compute (Dispatchers.Default).

Design choices (short) - Execution model:
Foreground Service (FGS) with a user-visible notification —
chosen because the task is long-running and interactive (start/stop by user). WorkManager alternative
is provided but commented. - Threading/backpressure: Producer (service) emits frame tokens at
target Hz to a bounded Channel . Consumer coroutines process tokens on Dispatchers.Default .
If channel is full, the producer drops frames to avoid main-thread blocking and unbounded queue
growth. - Battery awareness: If Battery Saver is ON, the service lowers frequency from 20Hz to 10Hz
and reduces compute load by 1 (min 1). Tiny banner in-app shows "Power-save mode" when active. -
Instrumentation: Jank measurement — the app logs frame latency and moving averages. The README
includes instructions to integrate Android's JankStats or use Android Studio Profiler/Perfetto for the
jank% requirement.
