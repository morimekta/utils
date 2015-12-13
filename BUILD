VERSION='0.1'

genrule(
    name = 'android-util',
    cmd = 'cp $(SRCS) $(OUTS)',
    srcs = ['//src:src'],
    outs = ['android-util-%s.jar' % VERSION]
)
