Buttons=[]
class Button:

    def __init__(self, x, y, w, h):
        self.position = np.asarray([x, y])
        self.size = np.asarray([w, h])
        self.inside = False
        Buttons.append(self)

    def show(self):
        global background
        cv2.rectangle(background, self.position, self.position+self.size, (255,255,255), 1)

    def isMouseIn(self, x, y):
        if x>self.position[0] and y>self.position[1] and x<self.position[0]+self.size[0] and y<self.position[1]+self.size[1]:
            if self.inside==False:
                cv2.rectangle(background, self.position, self.position+self.size, (255,255,255), 1)
                self.inside=True
        elif self.inside==True:
            cv2.rectangle(background, self.position, self.position+self.size, (255,0,0), 1)
            self.inside=False

    @classmethod
    def DrawButtons(clf):
        global Buttons
        for i in range(len(Buttons)):
            Buttons[i].show()

def onMouseClicked(event, x, y, flags, wtf):
    pass
    #if flags==1:


mybutton = Button(50, 50, 100, 100)

Button.DrawButtons()