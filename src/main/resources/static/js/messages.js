(function () {
    'use strict';

    // ── Constantes ─────────────────────────────────────────────
    const CSRF_TOKEN  = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const CSRF_HEADER = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // ── État ────────────────────────────────────────────────────
    var currentConvId   = null;
    var stompClient     = null;

    // ── Éléments DOM ───────────────────────────────────────────
    var sidebar         = document.getElementById('msgsSidebar');
    var panel           = document.getElementById('msgsPanel');
    var chatBox         = document.getElementById('chatBox');
    var panelHeader     = document.getElementById('panelHeader');
    var panelAvatar     = document.getElementById('panelAvatar');
    var panelName       = document.getElementById('panelName');
    var panelService    = document.getElementById('panelService');
    var chatInputBar    = document.getElementById('chatInputBar');
    var panelPlaceholder = document.getElementById('panelPlaceholder');
    var messageInput    = document.getElementById('messageInput');
    var sendBtn         = document.getElementById('sendBtn');
    var mobileBack      = document.getElementById('mobileBack');

    // ── Connexion STOMP ─────────────────────────────────────────
    stompClient = Stomp.over(new SockJS('/ws'));
    stompClient.debug = null;

    stompClient.connect({}, function () {
        // Canal privé : tous les nouveaux messages arrivent ici
        stompClient.subscribe('/user/queue/messages', function (frame) {
            handleIncoming(JSON.parse(frame.body));
        });

        // Ouvrir la conversation pré-sélectionnée côté serveur (si présente)
        if (INITIAL_CONV) {
            var item = document.querySelector('.conv-item[data-id="' + INITIAL_CONV + '"]');
            if (item) openConversation(item);
        }

    }, function () {
        showToast('Connexion perdue. Veuillez recharger la page.', true);
    });

    // ── Clic sur un item de la sidebar ─────────────────────────
    document.getElementById('convList').addEventListener('click', function (e) {
        var item = e.target.closest('.conv-item');
        if (!item) return;
        openConversation(item);
    });

    document.getElementById('convList').addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
            var item = e.target.closest('.conv-item');
            if (item) { e.preventDefault(); openConversation(item); }
        }
    });

    // ── Bouton retour mobile ────────────────────────────────────
    mobileBack.addEventListener('click', function () {
        panel.classList.remove('msgs-panel--visible');
        sidebar.classList.remove('msgs-sidebar--hidden');
    });

    // ── Ouvrir une conversation ─────────────────────────────────
    function openConversation(item) {
        var convId = parseInt(item.getAttribute('data-id'), 10);

        // Mettre à jour l'état actif dans la sidebar
        document.querySelectorAll('.conv-item--active').forEach(function (el) {
            el.classList.remove('conv-item--active');
        });
        item.classList.add('conv-item--active');

        // Supprimer le badge non-lu
        var badge = item.querySelector('.conv-item__badge');
        if (badge) badge.remove();

        // Remplir l'en-tête du panneau
        panelAvatar.textContent  = item.getAttribute('data-initial') || '?';
        panelName.textContent    = item.getAttribute('data-other')   || 'Conversation';
        panelService.textContent = item.getAttribute('data-service') || '';

        // Afficher le panneau, masquer le placeholder
        panelHeader.hidden      = false;
        chatInputBar.hidden     = false;
        panelPlaceholder.hidden = true;

        // Mobile : glisser le panneau en vue
        panel.classList.add('msgs-panel--visible');
        sidebar.classList.add('msgs-sidebar--hidden');

        // Charger l'historique si on change de conversation
        if (convId !== currentConvId) {
            currentConvId = convId;
            chatBox.innerHTML = '';
            loadHistory(convId).then(function () { markRead(convId); });
        }

        messageInput.focus();
    }

    // ── Chargement de l'historique ──────────────────────────────
    function loadHistory(convId) {
        return fetch('/api/conversations/' + convId + '/messages?page=0&size=50')
            .then(function (res) {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(function (page) {
                page.content.forEach(function (msg) { renderMessage(msg); });
            })
            .catch(function (err) {
                console.warn('Historique introuvable :', err.message);
            });
    }

    // ── Marquer comme lus ───────────────────────────────────────
    function markRead(convId) {
        if (!CSRF_TOKEN || !CSRF_HEADER) return;
        fetch('/api/conversations/' + convId + '/read', {
            method:  'PATCH',
            headers: { [CSRF_HEADER]: CSRF_TOKEN }
        }).catch(function () { /* fire-and-forget */ });
    }

    // ── Recevoir un message via WebSocket ───────────────────────
    function handleIncoming(msg) {
        if (msg.conversationId === currentConvId) {
            // La conversation est ouverte : afficher le message
            renderMessage(msg);
            markRead(currentConvId);
        } else {
            // Conversation en arrière-plan : incrémenter le badge
            var item = document.querySelector('.conv-item[data-id="' + msg.conversationId + '"]');
            if (!item) return;
            var badge = item.querySelector('.conv-item__badge');
            if (badge) {
                badge.textContent = parseInt(badge.textContent, 10) + 1;
            } else {
                badge = document.createElement('span');
                badge.className   = 'conv-item__badge';
                badge.textContent = '1';
                item.appendChild(badge);
            }
            // Mettre à jour l'aperçu
            var preview = item.querySelector('.conv-item__preview');
            if (preview) {
                var txt = msg.content.length > 60
                    ? msg.content.substring(0, 60) + '…'
                    : msg.content;
                preview.textContent = txt;
            }
        }
    }

    // ── Rendu d'un message ──────────────────────────────────────
    function renderMessage(msg) {
        var isMine = msg.senderUsername === CURRENT_USER;

        var wrap = document.createElement('div');
        wrap.className = 'chat-bubble-wrap ' +
                         (isMine ? 'chat-bubble-wrap--mine' : 'chat-bubble-wrap--theirs');

        if (!isMine) {
            var sender = document.createElement('p');
            sender.className   = 'chat-bubble__sender';
            sender.textContent = msg.senderUsername;
            wrap.appendChild(sender);
        }

        var bubble = document.createElement('div');
        bubble.className   = 'chat-bubble ' +
                             (isMine ? 'chat-bubble--mine' : 'chat-bubble--theirs');
        bubble.textContent = msg.content;
        wrap.appendChild(bubble);

        chatBox.appendChild(wrap);
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    // ── Envoi d'un message ──────────────────────────────────────
    function sendMessage() {
        if (!currentConvId || !stompClient) return;
        var content = messageInput.value.trim();
        if (!content) return;

        stompClient.send('/app/chat/' + currentConvId, {}, JSON.stringify({ content: content }));
        messageInput.value = '';
        messageInput.focus();
    }

    sendBtn.addEventListener('click', sendMessage);
    messageInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // ── Toast ───────────────────────────────────────────────────
    function showToast(message, isError) {
        var toast = document.createElement('div');
        toast.style.cssText =
            'position:fixed;bottom:80px;left:50%;transform:translateX(-50%);' +
            'background:' + (isError ? '#c13515' : '#222222') + ';color:#fff;' +
            'padding:8px 16px;border-radius:8px;font-size:14px;z-index:999;' +
            'max-width:320px;text-align:center;';
        toast.textContent = message;
        document.body.appendChild(toast);
        setTimeout(function () { toast.remove(); }, 4000);
    }

}());
