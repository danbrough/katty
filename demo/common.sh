
MODULE="${PWD##*/}"
cd ..
ROOT_DIR="$(pwd)"

[ -z  "$KLOG_COLOR" ] && KLOG_COLOR=1
[ -z "$KLOG_LEVEL" ] && KLOG_LEVEL=TRACE
export KLOG_COLOR KLOG_LEVEL



