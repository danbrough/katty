
MODULE="${PWD##*/}"
cd ..
ROOT_DIR="$(pwd)"
export GRADLE_USER_HOME="${GRADLE_USER_HOME="$HOME/.gradle"}"

[ -z  "$KLOG_COLOR" ] && KLOG_COLOR=1
[ -z "$KLOG_LEVEL" ] && KLOG_LEVEL=TRACE
export KLOG_COLOR KLOG_LEVEL



