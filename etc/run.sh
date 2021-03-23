#!/bin/bash
java -Djogl.disable.opengles=true -Xms2G -Xmx2G -XX:+UseZGC -jar hafen.jar -U https://game.havenandhearth.com/res/ game.havenandhearth.com
