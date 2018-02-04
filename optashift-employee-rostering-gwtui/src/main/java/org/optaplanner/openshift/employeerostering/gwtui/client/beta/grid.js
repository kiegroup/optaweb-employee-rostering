function scale(coordinate) {
    return Math.floor(coordinate - (coordinate % HANDLES.pixelSize()));
}

function drawGrid($viewport) {

    var $parent = $('<div />', {
        class: 'grid',
        width: $viewport.width(),
        height: $viewport.height()
    }).appendTo('.grid-container');

    for (var i = 0; i < HANDLES.gridSize($viewport); i += HANDLES.pixelSize()) {
        var $gridDiv = $('<div />');
        HANDLES.decorateGridDiv($gridDiv);
        $gridDiv.appendTo($parent);
    }
}

function toggleDraggablity($blob, $resize, $lock) {
    if ($lock.hasClass("locked")) {
        $blob.draggable('disable');
        $resize.draggable('disable');
    } else {
        $blob.draggable('enable');
        $resize.draggable('enable');
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

    var $resize = $('<div />', {class: 'resize hide'});
    $resize.appendTo($actions);
    HANDLES.decorateResizeHandle($blob, $resize);

    var $lock = $('<div />', {class: 'lock unlocked hide'});
    $lock.appendTo($actions);
    $lock.click(function (e) {
        $lock.toggleClass('locked');
        $lock.toggleClass('unlocked');
        toggleDraggablity($blob, $resize, $lock);
    });

    toggleDraggablity($blob, $resize, $lock);

    var $close = $('<div />', {class: 'close hide'});
    $close.appendTo($actions);
    $close.click(function (e) {
        $resize.draggable('disable');
        $resize.draggable("destroy");
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

        // Delete sub-lane (SHIFT + MIDDLE-CLICK or SHIFT + ALT + CLICK)
        if (e.shiftKey && e.button === 1 || e.shiftKey && e.altKey) {
            e.target.remove();
        }

        // Add sub-lane (SHIFT + CLICK)
        else if (e.shiftKey) {
            addSubLaneTo($(e.target).parent());
        }

        // Add blob (ALT + CLICK)
        else if (e.altKey) {
            addBlobTo($(e.target), {x: e.offsetX, y: e.offsetY});
        }
    });
}

function addSubLaneTo($target) {
    var $subLane = $('<div />', {class: 'sub-lane'});
    $subLane.appendTo($target);
    bindSubLaneEvents($subLane);
}

function addBlobTo($target, point) {
    var $blob = $('<div />', {class: 'blob'});
    $blob.appendTo($target);
    HANDLES.decorateNewBlob($blob, point);
    bindBlobEvents($blob);
}

$(".sub-lane").each(function () {
    bindSubLaneEvents($(this));
});

$(".blob").each(function () {
    bindBlobEvents($(this));
});

drawGrid($('.viewport'));