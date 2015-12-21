VERSION='0.1'

genrule(
    name = 'android-util',
    cmd = 'cp $(SRCS) $(OUTS)',
    srcs = ['//java:java'],
    outs = ['android-util-%s.jar' % VERSION]
)
