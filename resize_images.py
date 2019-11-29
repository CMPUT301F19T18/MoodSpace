import os

import PIL
from PIL import Image


EMOTE_FILES = (
    "original.png",
    "anger.png",
    "contempt.png",
    "disgust.png",
    "enjoyment.png",
    "fear.png",
    "sadness.png",
    "surprise.png",
    "tag.png",
    )

FILE_PATH = "./MoodSpace/app/src/main/res/drawable/"

#ORIGINAL_REGION = (372, 67)
NEW_REGION = (330, 67)

def main():
    for emote_file in EMOTE_FILES:
        image_path = os.path.join(FILE_PATH, emote_file)
        original_img = Image.open(image_path)
        # original region
        new_img = original_img.resize(NEW_REGION, resample=PIL.Image.CUBIC)
        new_img.save(image_path, "PNG")


if __name__ == "__main__":
    main()
