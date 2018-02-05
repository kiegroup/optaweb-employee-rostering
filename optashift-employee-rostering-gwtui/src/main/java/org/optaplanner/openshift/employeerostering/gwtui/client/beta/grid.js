function scale(coordinate) {
    return Math.floor(coordinate - (coordinate % HANDLES.pixelSize()));
}

function drawGrid($viewport) {

    var $gridContainer = $('<div />', {
        class: 'grid',
        width: $viewport.width(),
        height: $viewport.height()
    }).appendTo('.grid-container');

    $gridContainer.on("click", function (e) {
        if (e.altKey) {
            var $lane = addLaneTo($viewport);
            addSubLaneTo($lane);
        }
    });

    for (var i = 0; i < HANDLES.size($viewport); i += HANDLES.pixelSize()) {
        var $gridDiv = $('<div />');
        HANDLES.decorateGridDiv($gridDiv);
        $gridDiv.appendTo($gridContainer);
    }

}

function setLocked($blob, $lock, locked) {
    if (locked) {
        $lock.removeClass("unlocked");
        $lock.addClass("locked");
        $blob.draggable('disable');
        $blob.resizable('disable');
    } else {
        $lock.removeClass("locked");
        $lock.addClass("unlocked");
        $blob.resizable('enable');
        $blob.draggable('enable');
    }
}

function bindBlobEvents($blob) {

    $blob.draggable({
        addClasses: false,
        cancel: '.blob div',
        containment: $blob.parent(), //That's the sub-lane
        axis: HANDLES.grid.orientation,
        grid: [HANDLES.grid.pixel.width, HANDLES.grid.pixel.height],
        scroll: true,
        scrollSpeed: 10,
        scrollSensitivity: 50
    });

    var $actions = $('<div />', {class: 'actions'});
    $actions.appendTo($blob);

    HANDLES.configureResizability($blob);

    var $lock = $('<div />', {class: 'lock unlocked hide'});
    $lock.appendTo($actions);
    $lock.click(function (e) {
        $lock.toggleClass('locked');
        $lock.toggleClass('unlocked');
        setLocked($blob, $lock, $lock.hasClass("locked"));
    });

    var $close = $('<div />', {class: 'close hide'});
    $close.appendTo($actions);
    $close.click(function (e) {
        $blob.resizable('disable');
        $blob.resizable('destroy');
        $blob.draggable('disable');
        $blob.draggable("destroy");
        $blob.remove();
    });

    return $blob;
}

function bindSubLaneEvents($subLane) {

    $subLane.on("click", function (e) {

        if (e.target !== e.currentTarget) {
            return false;
        }

        var $target = $(e.target);

        // Delete sub-lane (SHIFT + MIDDLE-CLICK or SHIFT + ALT + CLICK)
        if (e.shiftKey && e.button === 1 || e.shiftKey && e.altKey) {
            if (!$target.parent().hasChildren) {
                $target.parent().remove();
            } else {
                $target.remove();
            }
        }

        // Add sub-lane (SHIFT + CLICK)
        else if (e.shiftKey) {
            addSubLaneTo($target.parent());
        }

        // Add blob (ALT + CLICK)
        else if (e.altKey) {
            var offset = HANDLES.grid.orientation === "y" ? e.offsetY : e.offsetX;
            addBlobTo($target, {
                label: "New",
                size: HANDLES.defaultBlobSize,
                position: offset / HANDLES.pixelSize(),
                locked: false
            });
        }
    });
}

function addLaneTo($target) {
    var $lane = $('<div />', {class: "lane"});
    $lane.appendTo($target);
    return $lane;
}

function addSubLaneTo($target) {
    var $subLane = $('<div />', {class: 'sub-lane'});
    $subLane.appendTo($target);
    bindSubLaneEvents($subLane);
    return $subLane;
}

function addBlobTo($target, blob) {
    var $blob = $('<div />', {class: 'blob'});
    $blob.appendTo($target);
    HANDLES.decorateNewBlob($blob, blob);
    $blob.prepend(blob.label);
    bindBlobEvents($blob);
    setLocked($blob, $blob.find(".lock"), blob.locked);
    return $blob;
}

var $viewport = $('.viewport');

drawGrid($viewport);

$(data.lanes).each(function (i, lane) {
    var $lane = addLaneTo($viewport);

    $(lane.subLanes).each(function (j, subLane) {
        var $subLane = addSubLaneTo($lane);

        $(subLane.blobs).each(function (k, blob) {
            addBlobTo($subLane, blob);
        });
    });
});