import { useSearchParams, useNavigate } from 'react-router-dom';
import { ModalId } from '../types';

export function useModal() {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();

  const activeModal = searchParams.get('modal') as ModalId | null;

  function openModal(id: ModalId) {
    setSearchParams({ modal: id }, { replace: false });
  }

  function closeModal() {
    navigate(-1);
  }

  function switchModal(id: ModalId) {
    setSearchParams({ modal: id }, { replace: true });
  }

  return { activeModal, openModal, closeModal, switchModal };
}
