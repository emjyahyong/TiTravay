(function () {
    'use strict';

    // ── Suppression de compte ─────────────────────────────────
    var btnOpen    = document.getElementById('btnOpenDelete');
    var modal      = document.getElementById('deleteModal');
    var btnClose   = document.getElementById('btnCloseDelete');
    var btnConfirm = document.getElementById('btnConfirmDelete');
    var confirmInput = document.getElementById('confirmUsername');

    if (btnOpen && modal) {
        btnOpen.addEventListener('click', function () {
            if (confirmInput) confirmInput.value = '';
            if (btnConfirm)   btnConfirm.disabled = true;
            modal.showModal();
        });
    }

    if (btnClose && modal) {
        btnClose.addEventListener('click', function () { modal.close(); });
    }

    // Active le bouton de confirmation seulement si le username correspond
    if (confirmInput && btnConfirm) {
        confirmInput.addEventListener('input', function () {
            btnConfirm.disabled = confirmInput.value !== EXPECTED_USERNAME;
        });
    }

    // Fermer la modale en cliquant sur le backdrop
    if (modal) {
        modal.addEventListener('click', function (e) {
            if (e.target === modal) modal.close();
        });
    }

    // ── Suppression de service ────────────────────────────────
    window.openServiceDeleteModal = function (btn) {
        var serviceModal = document.getElementById('serviceDeleteModal');
        var serviceForm  = document.getElementById('serviceDeleteForm');
        var serviceName  = document.getElementById('serviceModalName');
        if (!serviceModal) return;

        var id    = btn.getAttribute('data-id');
        var title = btn.getAttribute('data-title');
        if (serviceName) serviceName.textContent = title || 'ce service';
        if (serviceForm) serviceForm.action = '/compte/services/' + id + '/supprimer';

        serviceModal.showModal();
    };

    // Fermer la modale service via backdrop
    var serviceModal = document.getElementById('serviceDeleteModal');
    if (serviceModal) {
        serviceModal.addEventListener('click', function (e) {
            if (e.target === serviceModal) serviceModal.close();
        });
    }

    // ── Validation mot de passe en temps réel ─────────────────
    var newPwd     = document.getElementById('newPassword');
    var confirmPwd = document.getElementById('confirmPassword');
    var strengthHint = document.getElementById('strengthHint');
    var matchHint    = document.getElementById('matchHint');
    var submitBtn    = document.getElementById('submitPassword');

    var POLICY = /^(?=.*[A-Z])(?=.*\d).{8,}$/;

    function checkPolicy(val) {
        return POLICY.test(val);
    }

    function updateSubmitState() {
        if (!submitBtn) return;
        var policyOk = newPwd  && checkPolicy(newPwd.value);
        var matchOk  = confirmPwd && newPwd && confirmPwd.value === newPwd.value;
        submitBtn.disabled = !(policyOk && matchOk);
    }

    if (newPwd && strengthHint) {
        newPwd.addEventListener('input', function () {
            if (!newPwd.value) {
                strengthHint.textContent = '';
                strengthHint.className   = 'form-field__hint';
                newPwd.className         = 'form-field__input';
            } else if (checkPolicy(newPwd.value)) {
                strengthHint.textContent = '✓ Mot de passe valide';
                strengthHint.className   = 'form-field__hint form-field__hint--ok';
                newPwd.className         = 'form-field__input form-field__input--ok';
            } else {
                strengthHint.textContent = 'Au moins 8 caractères, une majuscule et un chiffre';
                strengthHint.className   = 'form-field__hint form-field__hint--error';
                newPwd.className         = 'form-field__input form-field__input--error';
            }
            updateSubmitState();
        });
    }

    if (confirmPwd && matchHint) {
        confirmPwd.addEventListener('input', function () {
            if (!confirmPwd.value) {
                matchHint.textContent = '';
                matchHint.className   = 'form-field__hint';
                confirmPwd.className  = 'form-field__input';
            } else if (newPwd && confirmPwd.value === newPwd.value) {
                matchHint.textContent = '✓ Les mots de passe correspondent';
                matchHint.className   = 'form-field__hint form-field__hint--ok';
                confirmPwd.className  = 'form-field__input form-field__input--ok';
            } else {
                matchHint.textContent = 'Les mots de passe ne correspondent pas';
                matchHint.className   = 'form-field__hint form-field__hint--error';
                confirmPwd.className  = 'form-field__input form-field__input--error';
            }
            updateSubmitState();
        });
    }

    // ── Auto-disparition des alertes ──────────────────────────
    document.querySelectorAll('.alert').forEach(function (el) {
        setTimeout(function () {
            el.style.transition = 'opacity 0.5s';
            el.style.opacity    = '0';
            setTimeout(function () { el.remove(); }, 500);
        }, 5000);
    });

}());
